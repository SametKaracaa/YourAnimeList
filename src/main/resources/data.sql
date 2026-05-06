-- ========================================================
-- ANIME LIST APP - BAŞLANGIÇ VERİLERİ (data.sql)
-- ========================================================
-- Spring Boot başlatıldığında otomatik çalışır
-- (spring.jpa.defer-datasource-initialization=true sayesinde
--  Hibernate şemayı oluşturduktan SONRA çalışır).
--
-- INSERT IGNORE kullanılır → kayıt zaten varsa hata atmaz, atlar.
-- Bu sayede ddl-auto=update modunda her başlatmada güvenle çalışır;
-- kullanıcının kendi eklediği veriler asla silinmez.
--
-- Şema (3 tablo):
--   kullanicilar             (id, kullanici_adi, sifre, email, rol)
--   animeler                 (id, anime_adi, tur, yayin_yili, studyo,
--                             toplam_bolum, kapak_gorseli, gorsel_tipi)
--   kullanici_anime_listesi  (id, kullanici_id, anime_id,
--                             izleme_durumu, puan, izlenen_bolum, eklenme_tarihi)
-- ========================================================


-- --- 1) TEST KULLANICISI ---
-- Şifre: "password" (BCrypt encoded)
INSERT IGNORE INTO kullanicilar (id, kullanici_adi, sifre, email, rol) VALUES
(1, 'testuser',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'testuser@example.com',
    'ROLE_USER');


-- --- 2) ANIME KATALOĞU (kullanıcıdan bağımsız) ---
INSERT IGNORE INTO animeler (id, anime_adi, tur, yayin_yili, studyo, toplam_bolum) VALUES
(1, 'Jujutsu Kaisen',
    'Aksiyon, Doğaüstü, Okul',
    2020, 'MAPPA', 24),

(2, 'Chainsaw Man',
    'Aksiyon, Karanlık Fantezi, Doğaüstü',
    2022, 'MAPPA', 12),

(3, 'JoJo''s Bizarre Adventure: Phantom Blood',
    'Aksiyon, Macera, Doğaüstü',
    2012, 'David Production', 9),

(4, 'Attack on Titan',
    'Aksiyon, Dram, Fantastik',
    2013, 'Wit Studio', 25),

(5, 'Frieren: Beyond Journey''s End',
    'Macera, Dram, Fantastik',
    2023, 'Madhouse', 28);


-- --- 3) KULLANICI <-> ANIME İLİŞKİLERİ (izleme durumu + puan + ilerleme) ---
-- (kullanici_id, anime_id) çifti UNIQUE → IGNORE güvenli.
INSERT IGNORE INTO kullanici_anime_listesi
    (kullanici_id, anime_id, izleme_durumu, puan, izlenen_bolum, eklenme_tarihi) VALUES
(1, 1, 'WATCHING',      9,    15,   NOW()),
(1, 2, 'COMPLETED',     8,    12,   NOW()),
(1, 3, 'COMPLETED',     9,    9,    NOW()),
(1, 4, 'COMPLETED',     10,   25,   NOW()),
(1, 5, 'PLAN_TO_WATCH', NULL, NULL, NOW());
