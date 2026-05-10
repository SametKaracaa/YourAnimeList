package com.animelistapp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
            }
        };
    }
}
