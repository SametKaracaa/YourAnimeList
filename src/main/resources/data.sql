INSERT IGNORE INTO kullanicilar (id, kullanici_adi, sifre, email, rol) VALUES
(1, 'testuser',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'testuser@example.com',
    'ROLE_USER');

INSERT IGNORE INTO animeler (id, anime_adi, tur, yayin_yili, studyo, toplam_bolum, kataloga_acik) VALUES
(1, 'Jujutsu Kaisen',
    'Aksiyon, Doğaüstü, Okul',
    2020, 'MAPPA', 24, TRUE),

(2, 'Chainsaw Man',
    'Aksiyon, Karanlık Fantezi, Doğaüstü',
    2022, 'MAPPA', 12, TRUE),

(3, 'JoJo''s Bizarre Adventure: Phantom Blood',
    'Aksiyon, Macera, Doğaüstü',
    2012, 'David Production', 9, TRUE),

(4, 'Attack on Titan',
    'Aksiyon, Dram, Fantastik',
    2013, 'Wit Studio', 25, TRUE),

(5, 'Frieren: Beyond Journey''s End',
    'Macera, Dram, Fantastik',
    2023, 'Madhouse', 28, TRUE);

UPDATE animeler SET kataloga_acik = TRUE WHERE id IN (1, 2, 3, 4, 5);

INSERT IGNORE INTO kullanici_anime_listesi
    (kullanici_id, anime_id, izleme_durumu, puan, izlenen_bolum, eklenme_tarihi) VALUES
(1, 1, 'WATCHING',      9,    15,   NOW()),
(1, 2, 'COMPLETED',     8,    12,   NOW()),
(1, 3, 'COMPLETED',     9,    9,    NOW()),
(1, 4, 'COMPLETED',     10,   25,   NOW()),
(1, 5, 'PLAN_TO_WATCH', NULL, NULL, NOW());
