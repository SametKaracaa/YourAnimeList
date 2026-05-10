package com.animelistapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Uygulamanın giriş noktası (main sınıfı).
 * 
 * @SpringBootApplication anotasyonu 3 şeyi bir arada yapar:
 * 1. @Configuration   -> Bu sınıf bir ayar (config) sınıfıdır.
 * 2. @EnableAutoConfiguration -> Spring Boot otomatik yapılandırma yapar.
 * 3. @ComponentScan   -> Bu paket ve alt paketlerdeki bileşenleri tarar.
 */
@SpringBootApplication
public class AnimeListApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnimeListApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner schemaFixer(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE animeler DROP COLUMN kullanici_id");
                System.out.println("========== BİLGİLENDİRME ==========");
                System.out.println("Eski 'kullanici_id' sütunu veritabanından başarıyla temizlendi.");
                System.out.println("===================================");
            } catch (Exception e) {
                // Sütun zaten silinmişse hatayı yut, uygulamayı durdurma
            }
        };
    }
}
