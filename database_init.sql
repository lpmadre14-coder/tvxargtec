-- Script de inicialización de Base de Datos para TVXargtec Online
-- Compatible con Go backend + Android app

CREATE DATABASE IF NOT EXISTS tvxargtec_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tvxargtec_db;

-- Tabla de Contenido (Canales, Películas, Series)
CREATE TABLE IF NOT EXISTS content (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    logo TEXT,
    category_id VARCHAR(50),
    is_live BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category_id),
    INDEX idx_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    plan_type VARCHAR(20) DEFAULT 'free',
    plan_expiry DATE,
    avatar_url TEXT,
    points INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_plan (plan_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de Planes VIP
CREATE TABLE IF NOT EXISTS vip_plans (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    duration_days INT NOT NULL,
    features JSON,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de Favoritos
CREATE TABLE IF NOT EXISTS favorites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    content_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_fav (user_id, content_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de Historial de Reproducción
CREATE TABLE IF NOT EXISTS watch_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    content_id VARCHAR(50) NOT NULL,
    progress INT DEFAULT 0,
    duration INT DEFAULT 0,
    watched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (content_id) REFERENCES content(id) ON DELETE CASCADE,
    INDEX idx_user_history (user_id, watched_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insertar Planes VIP por defecto
INSERT IGNORE INTO vip_plans (id, name, price, duration_days, features) VALUES
('free', 'Free', 0.00, 0, '["Canales básicos", "Calidad SD", "Anuncios"]'),
('basic', 'Básico', 4.99, 30, '["50+ canales", "Calidad HD", "Soporte email"]'),
('premium', 'Premium', 9.99, 30, '["300+ canales", "Calidad Full HD", "Sin anuncios", "Soporte prioritario"]'),
('family', 'Familiar', 14.99, 30, '["Todos los canales", "4K disponible", "5 dispositivos", "Sin anuncios", "Soporte 24/7"]');

-- Insertar canales desde canales.json (los primeros canales como ejemplo)
INSERT IGNORE INTO content (id, title, url, logo, category_id, is_live) VALUES
('1', '3ABN International Network', 'https://3abn.bozztv.com/3abn2/Int_live/smil:Int_live.smil/playlist.m3u8', 'https://i.imgur.com/IecOZHR.png', 'religious', TRUE),
('2', '6 Wise Tv (720p)', 'https://live.enhdtv.com:8081/8150/index.m3u8', 'https://jrlist70.pages.dev/list/wise.png', 'family', TRUE),
('3', '8NTV (1080p)', 'https://60417ddeaf0d9.streamlock.net/ntv/videontv/playlist.m3u8', 'https://i.imgur.com/7ecgGIG.png', 'general', TRUE),
('4', '13C (1080p)', 'https://origin.dpsgo.com/ssai/event/GI-9cp_bT8KcerLpZwkuhw/master.m3u8', 'https://i.imgur.com/Zfe2f5j.png', 'culture', TRUE),
('5', '13 Entretención (720p)', 'https://origin.dpsgo.com/ssai/event/BBp0VeP6QtOOlH8nu3bWTg/master.m3u8', 'https://i.imgur.com/utI1tJV.png', 'entertainment', TRUE),
('6', '13 Festival (1080p)', 'https://origin.dpsgo.com/ssai/event/Nftd0fM2SXasfDlRphvUsg/master.m3u8', 'https://i.imgur.com/Ymk6j5o.png', 'music', TRUE),
('7', '13 Humor (1080p)', 'https://origin.dpsgo.com/ssai/event/cKWySXKgSK-SzlJmESkOWw/master.m3u8', 'https://i.imgur.com/KvMuTzN.png', 'comedy', TRUE),
('8', '13 Kids (1080p)', 'https://origin.dpsgo.com/ssai/event/LhHrVtyeQkKZ-Ye_xEU75g/master.m3u8', 'https://i.imgur.com/8WJUbSD.png', 'kids', TRUE),
('9', '13 Prime (720p)', 'https://origin.dpsgo.com/ssai/event/p4mmBxEzSmKAxY1GusOHrw/master.m3u8', 'https://i.imgur.com/R6D228s.png', 'lifestyle', TRUE),
('10', '13 Realities (1080p)', 'https://origin.dpsgo.com/ssai/event/g7_JOM0ORki9SR5RKHe-Kw/master.m3u8', 'https://i.imgur.com/m0SuwMU.png', 'entertainment', TRUE),
('11', '13 Teleseries (720p)', 'https://origin.dpsgo.com/ssai/event/f4TrySe8SoiGF8Lu3EIq1g/master.m3u8', 'https://i.imgur.com/csBNi2L.png', 'series', TRUE),
('12', '15.1 BPB (720p)', 'https://v2.tustreaming.cl/enlacebpbtv/index.m3u8', 'https://i.imgur.com/hyxkJYV.png', 'religious', TRUE),
('13', '24/7 Canal de Noticias', 'https://panel.host-live.com:19360/cn247tv/cn247tv.m3u8', 'https://i.imgur.com/4hDCB1M.png', 'news', TRUE),
('14', '24/7 TV (1080p)', 'https://master.tucableip.com/eldebertv/index.m3u8', 'https://i.imgur.com/03YIXxd.png', 'news', TRUE),
('15', '36 TV Huacho (720p)', 'https://live-evg10.tv360.bitel.com.pe/bitel/36tvSRT/playlist.m3u8', 'https://i.imgur.com/F4NZM3s.jpeg', 'general', TRUE),
('16', 'A24 (720p)', 'https://g5.vxral-slo.transport.edge-access.net/a12/ngrp:a24-100056_all/playlist.m3u8?sense=true', 'https://i.imgur.com/LnXQkIU.png', 'news', TRUE),
('17', 'A24 Paraguay', 'http://45.184.109.10/live/69393EAE6ADBC65A68F942022362A202/6.m3u8', 'https://i.imgur.com/XWBmjBk.png', 'news', TRUE),
('18', 'A&E Latin America (1080p)', 'http://138.121.15.230:9002/A&E/index.m3u8', 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/df/A%26E_Network_logo.svg/960px-A%26E_Network_logo.svg.png', 'entertainment', TRUE),
('19', 'A&R Canal Adventista (720p)', 'http://51.222.9.192:3589/stream/play.m3u8', 'https://i.imgur.com/ejRJqSI.png', 'religious', TRUE),
('20', 'Abajo e La Linea TV', 'https://5ff3d9babae13.streamlock.net/ynfpncxxjg/ynfpncxxjg/playlist.m3u8', 'https://i.imgur.com/CK5DBuh.png', 'general', TRUE),
('21', 'ABC Teleshow (720p)', 'https://live-evg10.tv360.bitel.com.pe/bitel/abctv/playlist.m3u8', 'https://i.imgur.com/cF7gJ2Q.jpeg', 'general', TRUE),
('22', 'ABC-TV Paraguay', 'http://45.184.109.10/live/69393EAE6ADBC65A68F942022362A202/7.m3u8', 'https://i.imgur.com/OqfBMGy.png', 'news', TRUE),
('23', 'ABTelevision (720p)', 'https://live-evg8.tv360.bitel.com.pe/bitel/abctelevisionSRT/playlist.m3u8', 'https://i.imgur.com/tqMDscD.png', 'general', TRUE),
('24', 'ABTV Bariloche', 'https://videostream.shockmedia.com.ar/hls/abtvbariloche/abtvbariloche.m3u8', 'https://i.imgur.com/l4TSfDr.png', 'general', TRUE),
('25', 'Abya Yala TV', 'https://seo.tv.bo/tv/LIpSEO-TV-8.m3u8', 'https://abyayala.tv.bo/wp-content/uploads/2024/04/MOSCA-BLANCA-OK-308x189.png', 'general', TRUE),
('26', 'Acapulco Shore Pluto TV', 'https://jmp2.uk/plu-61a52615cbef2500072876e2.m3u8', 'https://images.pluto.tv/channels/61a52615cbef2500072876e2/colorLogoPNG_1756972226445.png', 'series', TRUE),
('27', 'Acento TV', 'https://acentotv01.streamprolive.com/hls/live.m3u8', 'https://i.imgur.com/jhiZfHf.png', 'news', TRUE),
('28', 'Activa TV (720p)', 'https://videoserver.tmcreativos.com:19360/mbcrtzxnxd/mbcrtzxnxd.m3u8', 'https://i.imgur.com/S69lDu4.png', 'religious', TRUE),
('29', 'Adesso TV (720p)', 'https://cdn.jmvstream.com/w/LVW-9715/LVW9715_12B26T62tm/playlist.m3u8', 'https://i.imgur.com/KgetM8j.png', 'general', TRUE),
('30', 'ADN 40 (720p)', 'https://mdstrm.com/live-stream-playlist/60b578b060947317de7b57ac.m3u8', 'https://i.imgur.com/Og17U9N.png', 'news', TRUE),
('31', 'Adoram TV (720p)', 'https://live20.bozztv.com/giatv/giatv-adoram/adoram/chunks.m3u8', 'https://i.imgur.com/uU01KHz.png', 'religious', TRUE),
('32', 'Adrenalina Pura TV', 'https://jmp2.uk/plu-61b790b985706b00072cb797.m3u8', 'https://i.imgur.com/Pvid2iH.png', 'movies', TRUE),
('33', 'Adrenalina Pura TV (720p)', 'https://jmp2.uk/plu-61b793ccf571b80007b7a610.m3u8', 'https://i.imgur.com/Pvid2iH.png', 'movies', TRUE),
('34', 'Agape TV (720p)', 'https://5fc584f3f19c9.streamlock.net/agape/smil:agape.smil/playlist.m3u8', 'https://i.imgur.com/DsGmrnc.png', 'general', TRUE),
('35', 'Agenda Minera TV', 'https://stv2.boliviaplay.com.bo/hls/stream.m3u8', 'https://i.imgur.com/hg2PiIn.png', 'business', TRUE),
('36', 'Agro TV (720p)', 'https://live-evg8.tv360.bitel.com.pe/bitel/agroSRT/playlist.m3u8', 'https://i.imgur.com/1uLGa3w.png', 'outdoor', TRUE),
('37', 'Agrotendencia TV (1080p)', 'https://5fc584f3f19c9.streamlock.net/agrotendencia/videoagrotendencia_hls1/playlist.m3u8', 'https://i.imgur.com/frd60hR.png', 'outdoor', TRUE),
('38', 'Aguacate TV (1080p)', 'https://streamtv.intervenhosting.net:3040/hybrid/play.m3u8', 'https://i.ibb.co/wpWBsgf/IMG-20230705-154622.jpg', 'entertainment', TRUE),
('39', 'Aire de Santa Fe (1080p)', 'https://unlimited1-us.dps.live/airedesantafetv/airedesantafetv.smil/playlist.m3u8', 'https://i.imgur.com/60vSWW0.png', 'general', TRUE),
('40', 'Al Jazeera English (1080p)', 'https://live-hls-apps-aje-fa.getaj.net/AJE/index.m3u8', 'https://i.imgur.com/7bRVpnu.png', 'news', TRUE),
('41', 'Albricias TV (720p)', 'https://live-evg10.tv360.bitel.com.pe/bitel/albriciastv/playlist.m3u8', 'https://i.imgur.com/NZxu3z8.jpeg', 'general', TRUE),
('42', 'Alcance FM PLAY TV', 'https://video.wilohosting.com:19360/alcancefmtv/alcancefmtv.m3u8', 'https://i.imgur.com/ymcWecA.png', 'music', TRUE),
('43', 'Alcance TV (720p)', 'https://5bf8041cb3fed.streamlock.net/AlcanceTV/AlcanceTV/playlist.m3u8', 'https://i.imgur.com/5nYjRlb.png', 'religious', TRUE),
('44', 'Alcance TV (720p)', 'https://rv100.globalhost1.com:3516/live/tjjqdqurlive.m3u8', 'https://i.imgur.com/veeuqS1.png', 'religious', TRUE),
('45', 'Alien Nation by DUST (1080p)', 'https://dqi7ayt2o24fn.cloudfront.net/playlist.m3u8', 'https://i.imgur.com/FxYhME9.png', 'movies', TRUE),
('46', 'Almaya TV (720p)', 'https://video.hostingcaaguazu.com:19360/almayatv/almayatv.m3u8', 'https://i.imgur.com/dDAf1nJ.png', 'general', TRUE),
('47', 'Alpha Channel (720p)', 'https://5b01a3d32b65c.streamlock.net:1936/tvalpha/tvalpha/playlist.m3u8', 'https://i.imgur.com/c1QqslA.png', 'general', TRUE),
('48', 'Alsacias Televisión (ATV | Canal 28) (720p)', 'https://s.emisoras.tv:8081/atv/index.m3u8', 'https://i.imgur.com/SbuD1UW.png', 'general', TRUE),
('49', 'Altura TV (720p)', 'https://live-evg10.tv360.bitel.com.pe/bitel/urbanatv/playlist.m3u8', 'https://i.imgur.com/U6GwIL7.jpeg', 'general', TRUE),
('50', 'Amaga Television (720p)', 'https://viewhn.com/amagatelevision/sdfUYWKegHKRPpSirG/playlist.m3u8', 'https://i.imgur.com/hvpbfzG.png', 'general', TRUE),
('51', 'A24 (Argentina)', 'https://g5.vxral-slo.transport.edge-access.net/a12/ngrp:a24-100056_all/playlist.m3u8?sense=true', 'https://i.imgur.com/LnXQkIU.png', 'news', TRUE),
('52', '13C (Chile)', 'https://origin.dpsgo.com/ssai/event/GI-9cp_bT8KcerLpZwkuhw/master.m3u8', 'https://i.imgur.com/Zfe2f5j.png', 'culture', TRUE),
('53', 'Pluto TV - Acapulco Shore', 'https://jmp2.uk/plu-61a52615cbef2500072876e2.m3u8', 'https://images.pluto.tv/channels/61a52615cbef2500072876e2/colorLogoPNG_1756972226445.png', 'entertainment', TRUE);
