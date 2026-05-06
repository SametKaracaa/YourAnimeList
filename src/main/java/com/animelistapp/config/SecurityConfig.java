package com.animelistapp.config;

import com.animelistapp.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security yapılandırma sınıfı.
 * 
 * Bu sınıf şunları ayarlar:
 * 1. Hangi sayfalara giriş yapmadan erişilebilir (giriş, kayıt, CSS dosyaları)
 * 2. Giriş formu ayarları (giriş sayfası URL'i, başarılı/başarısız yönlendirmeler)
 * 3. Çıkış (logout) ayarları
 * 4. Şifre şifreleme algoritması (BCrypt)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Güvenlik filtre zincirini yapılandırır.
     * HTTP isteklerinin hangi kurallara tabi olacağını belirler.
     */
    @Bean
    public SecurityFilterChain guvenlikFiltreZinciri(HttpSecurity http) throws Exception {

        http
            // Yetkilendirme kuralları
            .authorizeHttpRequests(yetki -> yetki
                // Bu yollara giriş yapmadan erişilebilir (public)
                .requestMatchers("/giris", "/kayit", "/css/**", "/js/**").permitAll()
                // Diğer tüm istekler için giriş yapmış olmak gerekir
                .anyRequest().authenticated()
            )

            // Form tabanlı giriş ayarları
            .formLogin(form -> form
                .loginPage("/giris")                          // Özel giriş sayfası URL'i
                .loginProcessingUrl("/giris-yap")             // Form POST edilecek URL
                .usernameParameter("kullaniciAdi")            // Form'daki kullanıcı adı alanının adı
                .passwordParameter("sifre")                   // Form'daki şifre alanının adı
                .defaultSuccessUrl("/anime/liste", true)      // Başarılı girişten sonra yönlendir
                .failureUrl("/giris?hata=true")               // Başarısız girişte geri dön
                .permitAll()
            )

            // Çıkış ayarları
            .logout(cikis -> cikis
                .logoutUrl("/cikis")                          // Çıkış URL'i
                .logoutSuccessUrl("/giris?cikis=true")        // Çıkış sonrası yönlendir
                .invalidateHttpSession(true)                  // Oturumu geçersiz kıl
                .deleteCookies("JSESSIONID")                  // Çerezleri temizle
                .permitAll()
            )

            // Kullanıcı detay servisini ayarla
            .userDetailsService(customUserDetailsService);

        return http.build();
    }

    /**
     * Şifre şifreleme bean'i.
     * BCrypt algoritması kullanılır - tek yönlü şifreleme yapar.
     * Aynı şifreyi her seferinde farklı şifreler (salt mekanizması).
     */
    @Bean
    public PasswordEncoder sifreEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Kimlik doğrulama yöneticisi bean'i.
     * Spring Security'nin giriş işlemlerini yönetmesi için gereklidir.
     */
    @Bean
    public AuthenticationManager kimlikDogrulamaYoneticisi(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
