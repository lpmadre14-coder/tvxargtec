package main

import (
	"compress/gzip"
	"database/sql"
	"encoding/json"
	"encoding/xml"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"strings"
	"sync"
	"time"

	_ "github.com/go-sql-driver/mysql"
	"golang.org/x/crypto/bcrypt"
)

// Estructuras de Datos
type Content struct {
	ID         string `json:"id"`
	Title      string `json:"title"`
	URL        string `json:"url"`
	Logo       string `json:"logo"`
	CategoryID string `json:"categoryId"`
	IsLive     bool   `json:"isLive"`
}

type User struct {
	ID         int       `json:"id"`
	Username   string    `json:"username"`
	Email      string    `json:"email"`
	Password   string    `json:"-"`
	PlanType   string    `json:"planType"`
	PlanExpiry time.Time `json:"planExpiry"`
	AvatarURL  string    `json:"avatarUrl"`
}

type LoginRequest struct {
	Email    string `json:"email"`
	Password string `json:"password"`
}

type RegisterRequest struct {
	Username string `json:"username"`
	Email    string `json:"email"`
	Password string `json:"password"`
}

// EPG (XMLTV) structures
type TV struct {
	XMLName    xml.Name      `xml:"tv"`
	Channels   []TVChannel   `xml:"channel"`
	Programmes []TVProgramme `xml:"programme"`
}

type TVChannel struct {
	ID          string `xml:"id,attr"`
	DisplayName string `xml:"display-name"`
	Icon        string `xml:"icon"`
}

type TVProgramme struct {
	Channel string `xml:"channel,attr"`
	Start   string `xml:"start,attr"`
	Stop    string `xml:"stop,attr"`
	Title   string `xml:"title"`
	Desc    string `xml:"desc"`
	Category string `xml:"category"`
}

type EpgCache struct {
	mu        sync.RWMutex
	data      *TV
	fetchedAt time.Time
	ttl       time.Duration
}

var epgCache = &EpgCache{ttl: 30 * time.Minute}

var db *sql.DB

func initDB() {
	dbUser := os.Getenv("DB_USER")
	dbPass := os.Getenv("DB_PASS")
	dbName := "tvxargtec_db"

	dsn := fmt.Sprintf("%s:%s@tcp(127.0.0.1:3306)/%s?parseTime=true", dbUser, dbPass, dbName)
	var err error
	db, err = sql.Open("mysql", dsn)
	if err != nil {
		log.Fatal(err)
	}

	if err := db.Ping(); err != nil {
		log.Printf("⚠️ No se pudo conectar a MySQL: %v. Usando modo mock.", err)
	} else {
		fmt.Println("✅ Conectado a MySQL con éxito")
	}
}

var epgSources = []string{
	"https://epgshare01.online/epgshare01/epg_ripper_ALL_SOURCES1.xml.gz",
	"https://epgshare01.online/epgshare01/epg_ripper_AR1.xml.gz",
	"https://epgshare01.online/epgshare01/epg_ripper_ES1.xml.gz",
	"https://epgshare01.online/epgshare01/epg_ripper_CL1.xml.gz",
	"https://epgshare01.online/epgshare01/epg_ripper_MX1.xml.gz",
	"https://epgshare01.online/epgshare01/epg_ripper_US1.xml.gz",
	"https://epgshare01.online/epgshare01/epg_ripper_UK1.xml.gz",
}

func fetchAndParseEPG() (*TV, error) {
	tv := &TV{}
	for _, url := range epgSources {
		data, err := downloadEPG(url)
		if err != nil {
			log.Printf("⚠️ EPG fetch error for %s: %v", url, err)
			continue
		}
		var partial TV
		if err := xml.Unmarshal(data, &partial); err != nil {
			log.Printf("⚠️ EPG parse error for %s: %v", url, err)
			continue
		}
		tv.Channels = append(tv.Channels, partial.Channels...)
		tv.Programmes = append(tv.Programmes, partial.Programmes...)
		log.Printf("✅ EPG loaded %d programmes from %s", len(partial.Programmes), url)
	}
	return tv, nil
}

func downloadEPG(url string) ([]byte, error) {
	client := &http.Client{Timeout: 30 * time.Second}
	req, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("User-Agent", "TVXargtec/1.0")
	resp, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	reader, err := gzip.NewReader(resp.Body)
	if err != nil {
		return nil, err
	}
	defer reader.Close()
	return io.ReadAll(reader)
}

func getEPGData() *TV {
	epgCache.mu.RLock()
	if epgCache.data != nil && time.Since(epgCache.fetchedAt) < epgCache.ttl {
		defer epgCache.mu.RUnlock()
		return epgCache.data
	}
	epgCache.mu.RUnlock()

	epgCache.mu.Lock()
	defer epgCache.mu.Unlock()

	// Double-check after acquiring write lock
	if epgCache.data != nil && time.Since(epgCache.fetchedAt) < epgCache.ttl {
		return epgCache.data
	}

	log.Println("🔄 Fetching EPG data...")
	tv, err := fetchAndParseEPG()
	if err != nil {
		log.Printf("⚠️ EPG fetch failed: %v", err)
		if epgCache.data != nil {
			return epgCache.data // serve stale cache
		}
		return &TV{}
	}
	epgCache.data = tv
	epgCache.fetchedAt = time.Now()
	log.Printf("✅ EPG cache refreshed: %d channels, %d programmes", len(tv.Channels), len(tv.Programmes))
	return tv
}

func findCurrentProgramme(programmes []TVProgramme, channelID string, now time.Time) *TVProgramme {
	for _, p := range programmes {
		if p.Channel != channelID {
			continue
		}
		start, err := parseXMLTVTime(p.Start)
		if err != nil {
			continue
		}
		stop, err := parseXMLTVTime(p.Stop)
		if err != nil {
			continue
		}
		if (now.Equal(start) || now.After(start)) && now.Before(stop) {
			return &p
		}
	}
	return nil
}

func findNextProgramme(programmes []TVProgramme, channelID string, now time.Time) *TVProgramme {
	for _, p := range programmes {
		if p.Channel != channelID {
			continue
		}
		start, err := parseXMLTVTime(p.Start)
		if err != nil {
			continue
		}
		if start.After(now) {
			return &p
		}
	}
	return nil
}

func parseXMLTVTime(t string) (time.Time, error) {
	// XMLTV format: 20260101000000 +0000
	t = strings.TrimSpace(t)
	parts := strings.Split(t, " ")
	if len(parts) < 1 {
		return time.Time{}, fmt.Errorf("invalid time format")
	}
	timeStr := parts[0]
	offset := "+0000"
	if len(parts) > 1 {
		offset = parts[1]
	}
	parsed, err := time.Parse("20060102150405 -0700", timeStr+" "+offset)
	if err != nil {
		return time.Parse("20060102150405 -0700", timeStr+" +0000")
	}
	return parsed, nil
}

func getChannelIDMap(tv *TV) map[string]string {
	m := make(map[string]string)
	for _, ch := range tv.Channels {
		m[ch.ID] = ch.DisplayName
		// Also map by display name (case-insensitive)
		m[strings.ToLower(ch.DisplayName)] = ch.ID
	}
	return m
}

func main() {
	initDB()
	defer db.Close()

	// Rutas API
	http.HandleFunc("/api/health", healthCheckHandler)
	http.HandleFunc("/api/content", contentHandler)
	http.HandleFunc("/api/epg", epgHandler)
	http.HandleFunc("/api/login", loginHandler)
	http.HandleFunc("/api/register", registerHandler)
	http.HandleFunc("/api/profile", profileHandler)
	http.HandleFunc("/api/vip/plans", vipPlansHandler)
	http.HandleFunc("/api/favorites", favoritesHandler)
	http.HandleFunc("/api/history", historyHandler)

	fmt.Println("🚀 Servidor TVXargtec corriendo en http://localhost:8081")
	log.Fatal(http.ListenAndServe(":8081", nil))
}

// Handlers
func healthCheckHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]string{"status": "ok", "engine": "Go + MySQL"})
}

func epgHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	tv := getEPGData()

	channelID := r.URL.Query().Get("channel_id")
	channelName := r.URL.Query().Get("channel_name")

	if channelID == "" && channelName == "" {
		// Return summary: all channels with current programme
		now := time.Now().UTC()
		type ChannelEpg struct {
			ID      string         `json:"id"`
			Name    string         `json:"name"`
			Current *TVProgramme   `json:"current,omitempty"`
			Next    *TVProgramme   `json:"next,omitempty"`
		}
		var result []ChannelEpg
		for _, ch := range tv.Channels {
			ce := ChannelEpg{ID: ch.ID, Name: ch.DisplayName}
			ce.Current = findCurrentProgramme(tv.Programmes, ch.ID, now)
			ce.Next = findNextProgramme(tv.Programmes, ch.ID, now)
			result = append(result, ce)
		}
		if result == nil {
			result = []ChannelEpg{}
		}
		json.NewEncoder(w).Encode(map[string]interface{}{
			"code":    200,
			"total":   len(result),
			"channels": result,
		})
		return
	}

	// Find channel by ID or name
	now := time.Now().UTC()
	ids := []string{channelID}
	if channelName != "" {
		lower := strings.ToLower(channelName)
		for _, ch := range tv.Channels {
			if strings.Contains(strings.ToLower(ch.DisplayName), lower) {
				ids = append(ids, ch.ID)
			}
		}
	}

	var result []map[string]interface{}
	for _, id := range ids {
		if id == "" {
			continue
		}
		current := findCurrentProgramme(tv.Programmes, id, now)
		next := findNextProgramme(tv.Programmes, id, now)
		entry := map[string]interface{}{
			"channelId": id,
			"current":   nil,
			"next":      nil,
		}
		if current != nil {
			entry["current"] = map[string]interface{}{
				"title":    current.Title,
				"desc":     current.Desc,
				"category": current.Category,
				"start":    current.Start,
				"stop":     current.Stop,
			}
		}
		if next != nil {
			entry["next"] = map[string]interface{}{
				"title":    next.Title,
				"desc":     next.Desc,
				"category": next.Category,
				"start":    next.Start,
				"stop":     next.Stop,
			}
		}
		result = append(result, entry)
	}
	if result == nil {
		result = []map[string]interface{}{}
	}
	json.NewEncoder(w).Encode(map[string]interface{}{
		"code":  200,
		"data":  result,
	})
}

func contentHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	rows, err := db.Query("SELECT id, title, url, logo, category_id, is_live FROM content")
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	defer rows.Close()

	var contents []Content
	for rows.Next() {
		var c Content
		if err := rows.Scan(&c.ID, &c.Title, &c.URL, &c.Logo, &c.CategoryID, &c.IsLive); err != nil {
			continue
		}
		contents = append(contents, c)
	}
	json.NewEncoder(w).Encode(contents)
}

func loginHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Método no permitido", http.StatusMethodNotAllowed)
		return
	}

	var req LoginRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "JSON inválido", http.StatusBadRequest)
		return
	}

	var user User
	err := db.QueryRow("SELECT id, username, email, password_hash, plan_type, plan_expiry, avatar_url FROM users WHERE email = ?", req.Email).
		Scan(&user.ID, &user.Username, &user.Email, &user.Password, &user.PlanType, &user.PlanExpiry, &user.AvatarURL)

	if err != nil {
		http.Error(w, "Usuario o contraseña incorrectos", http.StatusUnauthorized)
		return
	}

	// Verificar password (bcrypt)
	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(req.Password)); err != nil {
		http.Error(w, "Usuario o contraseña incorrectos", http.StatusUnauthorized)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"token": fmt.Sprintf("tvx-jwt-%d-%d", user.ID, time.Now().Unix()),
		"user": map[string]interface{}{
			"id":         user.ID,
			"username":   user.Username,
			"email":      user.Email,
			"planType":   user.PlanType,
			"planExpiry": user.PlanExpiry.Format("2006-01-02"),
			"avatarUrl":  user.AvatarURL,
		},
	})
}

func registerHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Método no permitido", http.StatusMethodNotAllowed)
		return
	}

	var req RegisterRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "JSON inválido", http.StatusBadRequest)
		return
	}

	// Hashear password
	hashedPassword, _ := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)

	_, err := db.Exec("INSERT INTO users (username, email, password_hash, plan_type, plan_expiry) VALUES (?, ?, ?, 'free', ?)",
		req.Username, req.Email, string(hashedPassword), time.Now().AddDate(0, 1, 0))

	if err != nil {
		http.Error(w, "Error al registrar usuario (Email ya existe)", http.StatusConflict)
		return
	}

	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(map[string]string{"message": "Usuario creado con éxito"})
}

func vipPlansHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	rows, err := db.Query("SELECT id, name, price, duration_days, features FROM vip_plans WHERE is_active = TRUE")
	if err != nil {
		json.NewEncoder(w).Encode([]map[string]interface{}{})
		return
	}
	defer rows.Close()

	var plans []map[string]interface{}
	for rows.Next() {
		var id, name string
		var price float64
		var durationDays int
		var featuresJSON string
		if err := rows.Scan(&id, &name, &price, &durationDays, &featuresJSON); err != nil {
			continue
		}
		var features []string
		json.Unmarshal([]byte(featuresJSON), &features)
		plans = append(plans, map[string]interface{}{
			"id":       id,
			"name":     name,
			"price":    price,
			"duration": fmt.Sprintf("%d días", durationDays),
			"features": features,
		})
	}
	json.NewEncoder(w).Encode(map[string]interface{}{
		"code": 200,
		"data": plans,
	})
}

func favoritesHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	userID := extractUserID(r)
	if userID == "" {
		http.Error(w, "No autorizado", http.StatusUnauthorized)
		return
	}

	switch r.Method {
	case http.MethodGet:
		rows, err := db.Query(`
			SELECT c.id, c.title, c.logo, f.created_at
			FROM favorites f
			JOIN content c ON f.content_id = c.id
			WHERE f.user_id = ?
			ORDER BY f.created_at DESC`, userID)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		defer rows.Close()

		var favs []map[string]interface{}
		for rows.Next() {
			var id, title, logo, addedAt string
			rows.Scan(&id, &title, &logo, &addedAt)
			favs = append(favs, map[string]interface{}{
				"channelId":    id,
				"channelTitle": title,
				"logo":         logo,
				"addedAt":      addedAt,
			})
		}
		json.NewEncoder(w).Encode(map[string]interface{}{"code": 200, "data": favs})

	case http.MethodPost:
		var body struct {
			ContentID string `json:"contentId"`
		}
		if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
			http.Error(w, "JSON inválido", http.StatusBadRequest)
			return
		}
		db.Exec("INSERT IGNORE INTO favorites (user_id, content_id) VALUES (?, ?)", userID, body.ContentID)
		json.NewEncoder(w).Encode(map[string]string{"message": "Agregado a favoritos"})

	case http.MethodDelete:
		contentID := strings.TrimPrefix(r.URL.Path, "/api/favorites/")
		db.Exec("DELETE FROM favorites WHERE user_id = ? AND content_id = ?", userID, contentID)
		json.NewEncoder(w).Encode(map[string]string{"message": "Eliminado de favoritos"})
	}
}

func historyHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	userID := extractUserID(r)
	if userID == "" {
		http.Error(w, "No autorizado", http.StatusUnauthorized)
		return
	}

	switch r.Method {
	case http.MethodGet:
		rows, err := db.Query(`
			SELECT c.id, c.title, wh.watched_at, wh.duration
			FROM watch_history wh
			JOIN content c ON wh.content_id = c.id
			WHERE wh.user_id = ?
			ORDER BY wh.watched_at DESC
			LIMIT 50`, userID)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}
		defer rows.Close()

		var history []map[string]interface{}
		for rows.Next() {
			var id, title, watchedAt string
			var duration int
			rows.Scan(&id, &title, &watchedAt, &duration)
			history = append(history, map[string]interface{}{
				"channelId":    id,
				"channelTitle": title,
				"watchedAt":    watchedAt,
				"duration":     duration,
			})
		}
		json.NewEncoder(w).Encode(map[string]interface{}{"code": 200, "data": history})

	case http.MethodPost:
		if strings.HasSuffix(r.URL.Path, "/clear/all") {
			db.Exec("DELETE FROM watch_history WHERE user_id = ?", userID)
			json.NewEncoder(w).Encode(map[string]string{"message": "Historial limpiado"})
			return
		}
		var body struct {
			ContentID string `json:"contentId"`
			Progress  int    `json:"progress"`
		}
		if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
			http.Error(w, "JSON inválido", http.StatusBadRequest)
			return
		}
		db.Exec("INSERT INTO watch_history (user_id, content_id, progress) VALUES (?, ?, ?)", userID, body.ContentID, body.Progress)
		json.NewEncoder(w).Encode(map[string]string{"message": "Historial registrado"})

	case http.MethodDelete:
		contentID := strings.TrimPrefix(r.URL.Path, "/api/history/")
		db.Exec("DELETE FROM watch_history WHERE user_id = ? AND content_id = ?", userID, contentID)
		json.NewEncoder(w).Encode(map[string]string{"message": "Eliminado del historial"})
	}
}

func extractUserID(r *http.Request) string {
	auth := r.Header.Get("Authorization")
	if auth == "" || !strings.HasPrefix(auth, "Bearer ") {
		return ""
	}
	parts := strings.Split(auth, " ")
	tokenParts := strings.Split(parts[1], "-")
	if len(tokenParts) < 3 {
		return ""
	}
	return tokenParts[2]
}

func profileHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")

	// Extraer token del header Authorization
	auth := r.Header.Get("Authorization")
	if auth == "" || !strings.HasPrefix(auth, "Bearer ") {
		http.Error(w, "Token requerido", http.StatusUnauthorized)
		return
	}

	// Parsear user ID del token (formato: tvx-jwt-{id}-{timestamp})
	parts := strings.Split(auth, " ")
	tokenParts := strings.Split(parts[1], "-")
	if len(tokenParts) < 3 {
		http.Error(w, "Token inválido", http.StatusUnauthorized)
		return
	}
	userID := tokenParts[2]

	var user User
	err := db.QueryRow("SELECT id, username, email, plan_type, plan_expiry, avatar_url FROM users WHERE id = ?", userID).
		Scan(&user.ID, &user.Username, &user.Email, &user.PlanType, &user.PlanExpiry, &user.AvatarURL)

	if err != nil {
		http.Error(w, "Usuario no encontrado", http.StatusNotFound)
		return
	}

	json.NewEncoder(w).Encode(map[string]interface{}{
		"user": map[string]interface{}{
			"id":         user.ID,
			"username":   user.Username,
			"email":      user.Email,
			"planType":   user.PlanType,
			"planExpiry": user.PlanExpiry.Format("2006-01-02"),
			"avatarUrl":  user.AvatarURL,
		},
	})
}
