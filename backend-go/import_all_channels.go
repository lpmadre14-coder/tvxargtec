package main

import (
	"bufio"
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"path/filepath"
	"strings"

	_ "github.com/go-sql-driver/mysql"
)

type Channel struct {
	ID         string `json:"id"`
	Title      string `json:"title"`
	URL        string `json:"url"`
	Logo       string `json:"logo"`
	CategoryID string `json:"categoryId"`
	IsLive     bool   `json:"isLive"`
}

var channelID int
var db *sql.DB

var countryCategoryMap = map[string]string{
	"albania":              "albania",
	"andorra":              "andorra",
	"argentina":            "argentina",
	"armenia":              "armenia",
	"australia":            "australia",
	"austria":              "austria",
	"azerbaijan":           "azerbaijan",
	"belarus":              "belarus",
	"belgium":              "belgium",
	"bosnia_and_herzegovina": "bosnia",
	"brazil":               "brazil",
	"bulgaria":             "bulgaria",
	"canada":               "canada",
	"chad":                 "chad",
	"chile":                "chile",
	"china":                "china",
	"costa_rica":           "costa_rica",
	"croatia":              "croatia",
	"cyprus":               "cyprus",
	"czech_republic":       "czech",
	"denmark":              "denmark",
	"dominican_republic":   "dominican",
	"egypt":                "egypt",
	"estonia":              "estonia",
	"faroe_islands":        "faroe",
	"finland":              "finland",
	"france":               "france",
	"georgia":              "georgia",
	"germany":              "germany",
	"greece":               "greece",
	"greenland":            "greenland",
	"hong_kong":            "hongkong",
	"hungary":              "hungary",
	"iceland":              "iceland",
	"india":                "india",
	"indonesia":            "indonesia",
	"iran":                 "iran",
	"iraq":                 "iraq",
	"ireland":              "ireland",
	"israel":               "israel",
	"italy":                "italy",
	"japan":                "japan",
	"korea":                "korea",
	"kosovo":               "kosovo",
	"latvia":               "latvia",
	"lithuania":            "lithuania",
	"luxembourg":           "luxembourg",
	"macau":                "macau",
	"malta":                "malta",
	"mexico":               "mexico",
	"moldova":              "moldova",
	"monaco":               "monaco",
	"montenegro":           "montenegro",
	"netherlands":          "netherlands",
	"north_korea":          "north_korea",
	"north_macedonia":      "north_macedonia",
	"norway":               "norway",
	"paraguay":             "paraguay",
	"peru":                 "peru",
	"poland":               "poland",
	"portugal":             "portugal",
	"qatar":                "qatar",
	"romania":              "romania",
	"russia":               "russia",
	"san_marino":           "san_marino",
	"saudi_arabia":         "saudi_arabia",
	"serbia":               "serbia",
	"slovakia":             "slovakia",
	"slovenia":             "slovenia",
	"somalia":              "somalia",
	"spain":                "spain",
	"spain_vod":            "spain",
	"sweden":               "sweden",
	"switzerland":          "switzerland",
	"taiwan":               "taiwan",
	"trinidad":             "trinidad",
	"turkey":               "turkey",
	"uk":                   "uk",
	"ukraine":              "ukraine",
	"united_arab_emirates": "uae",
	"usa":                  "usa",
	"usa_vod":              "usa",
	"venezuela":            "venezuela",
}

func main() {
	dbUser := os.Getenv("DB_USER")
	dbPass := os.Getenv("DB_PASS")
	if dbUser == "" {
		dbUser = "root"
	}

	dsn := fmt.Sprintf("%s:%s@tcp(127.0.0.1:3306)/tvxargtec_db?parseTime=true&multiStatements=true", dbUser, dbPass)
	var err error
	db, err = sql.Open("mysql", dsn)
	if err != nil {
		log.Fatal("Error conectando a MySQL:", err)
	}
	if err := db.Ping(); err != nil {
		log.Fatal("No se pudo conectar a MySQL:", err)
	}
	fmt.Println("✅ Conectado a MySQL")

	// Obtener el máximo ID actual
	row := db.QueryRow("SELECT COALESCE(MAX(CAST(id AS UNSIGNED)), 0) FROM content")
	row.Scan(&channelID)
	fmt.Printf("ID actual máximo: %d\n", channelID)

	playlistsDir := "IPTV-master/playlists"
	if len(os.Args) > 1 {
		playlistsDir = os.Args[1]
	}

	files, err := filepath.Glob(filepath.Join(playlistsDir, "playlist_*.m3u8"))
	if err != nil {
		log.Fatal("Error leyendo playlists:", err)
	}

	totalImported := 0
	for _, file := range files {
		base := filepath.Base(file)
		name := strings.TrimPrefix(base, "playlist_")
		name = strings.TrimSuffix(name, ".m3u8")

		categoryID := countryCategoryMap[name]
		if categoryID == "" {
			// Mapeo por prefijo zz_
			if strings.HasPrefix(name, "zz_") {
				categoryID = mapZZCategory(name)
			} else {
				categoryID = name
			}
		}

		count, err := importPlaylist(file, categoryID)
		if err != nil {
			fmt.Printf("⚠️ Error en %s: %v\n", base, err)
			continue
		}
		totalImported += count
		fmt.Printf("📺 %s → %d canales (categoría: %s)\n", base, count, categoryID)
	}

	fmt.Printf("\n✨ Total importados: %d canales\n", totalImported)
}

func mapZZCategory(name string) string {
	switch name {
	case "zz_movies":
		return "movies"
	case "zz_news_ar":
		return "news_ar"
	case "zz_news_en":
		return "news_en"
	case "zz_news_es":
		return "news_es"
	case "zz_documentaries_ar":
		return "documentaries"
	case "zz_documentaries_en":
		return "documentaries"
	case "zz_vod_it":
		return "vod_it"
	default:
		return name
	}
}

func importPlaylist(filePath, categoryID string) (int, error) {
	f, err := os.Open(filePath)
	if err != nil {
		return 0, err
	}
	defer f.Close()

	scanner := bufio.NewScanner(f)
	var channels []Channel
	var extinf string

	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if line == "" {
			continue
		}

		if strings.HasPrefix(line, "#EXTINF:") {
			extinf = line
		} else if !strings.HasPrefix(line, "#") && extinf != "" {
			url := line
			title := extractTitle(extinf)
			if title == "" || url == "" {
				extinf = ""
				continue
			}
			if strings.HasPrefix(url, "http") {
				channelID++
				logo := extractAttr(extinf, "tvg-logo")
				isLive := !strings.Contains(categoryID, "vod") &&
					!strings.Contains(categoryID, "movies") &&
					!strings.Contains(filePath, "_vod")

				channels = append(channels, Channel{
					ID:         fmt.Sprintf("%d", channelID),
					Title:      sanitize(title),
					URL:        url,
					Logo:       logo,
					CategoryID: categoryID,
					IsLive:     isLive,
				})
			}
			extinf = ""
		}
	}

	if len(channels) == 0 {
		return 0, nil
	}

	// Batch INSERT
	insertSQL := "INSERT IGNORE INTO content (id, title, url, logo, category_id, is_live) VALUES "
	var values []string
	var args []interface{}

	for _, ch := range channels {
		values = append(values, "(?, ?, ?, ?, ?, ?)")
		args = append(args, ch.ID, ch.Title, ch.URL, ch.Logo, ch.CategoryID, ch.IsLive)
	}

	insertSQL += strings.Join(values, ", ")
	_, err = db.Exec(insertSQL, args...)
	if err != nil {
		return 0, fmt.Errorf("INSERT error: %v", err)
	}

	return len(channels), nil
}

func extractTitle(extinf string) string {
	// EXTINF format: #EXTINF:-1 tvg-name="Title" ...,Title
	if idx := strings.LastIndex(extinf, ","); idx >= 0 {
		title := strings.TrimSpace(extinf[idx+1:])
		title = strings.Trim(title, "\"")
		return title
	}
	return ""
}

func extractAttr(extinf, attr string) string {
	search := attr + "=\""
	idx := strings.Index(extinf, search)
	if idx < 0 {
		return ""
	}
	start := idx + len(search)
	end := strings.Index(extinf[start:], "\"")
	if end < 0 {
		return ""
	}
	return extinf[start : start+end]
}

func sanitize(s string) string {
	s = strings.ReplaceAll(s, "\"", "")
	s = strings.ReplaceAll(s, "'", "")
	s = strings.TrimSpace(s)
	// Clean unicode replacement chars
	s = strings.Map(func(r rune) rune {
		if r == 0xFFFD || r == 0xFFFE || r == 0xFFFF {
			return -1
		}
		return r
	}, s)
	return s
}
