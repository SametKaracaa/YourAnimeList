package com.animelistapp.config;

import com.animelistapp.entity.User;
import com.animelistapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class AdminDataInitializer {

    @Bean
    public CommandLineRunner adminKullanicisiOlustur(UserRepository userRepository,
                                                     PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<User> mevcut = userRepository.findByKullaniciAdi("admin");
            if (mevcut.isPresent()) {
                User admin = mevcut.get();
                if (!admin.getRol().equals("ROLE_ADMIN")) {
                    admin.setRol("ROLE_ADMIN");
                }
                admin.setSifre(passwordEncoder.encode("admin123"));
                userRepository.save(admin);
            } else {
                User admin = new User();
                admin.setKullaniciAdi("admin");
                admin.setSifre(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@example.com");
                admin.setRol("ROLE_ADMIN");
                userRepository.save(admin);
            }
        };
    }
}
