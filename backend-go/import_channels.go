package main

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"os"

	_ "github.com/go-sql-driver/mysql"
)

type Content struct {
	ID         string `json:"id"`
	Title      string `json:"title"`
	URL        string `json:"url"`
	Logo       string `json:"logo"`
	CategoryID string `json:"categoryId"`
	IsLive     bool   `json:"isLive"`
}

func main() {
	// 1. Leer el archivo JSON
	data, err := os.ReadFile("canales.json")
	if err != nil {
		log.Fatalf("Error leyendo JSON: %v", err)
	}

	var channels []Content
	if err := json.Unmarshal(data, &channels); err != nil {
		log.Fatalf("Error procesando JSON: %v", err)
	}

	// 2. Conectar a MySQL
	dbUser := os.Getenv("DB_USER")
	dbPass := os.Getenv("DB_PASS")
	dsn := fmt.Sprintf("%s:%s@tcp(127.0.0.1:3306)/tvxargtec_db", dbUser, dbPass)
	
	db, err := sql.Open("mysql", dsn)
	if err != nil {
		log.Fatal(err)
	}
	defer db.Close()

	// 3. Insertar canales
	fmt.Printf("📥 Iniciando importación de %d canales...\n", len(channels))
	
	stmt, err := db.Prepare("INSERT INTO content (id, title, url, logo, category_id, is_live) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE title=VALUES(title), url=VALUES(url), logo=VALUES(logo)")
	if err != nil {
		log.Fatal(err)
	}
	defer stmt.Close()

	for _, c := range channels {
		_, err := stmt.Exec(c.ID, c.Title, c.URL, c.Logo, c.CategoryID, c.IsLive)
		if err != nil {
			log.Printf("❌ Error insertando canal %s: %v", c.Title, err)
		} else {
			fmt.Printf("✅ Canal importado: %s\n", c.Title)
		}
	}

	fmt.Println("🚀 Importación finalizada con éxito.")
}
