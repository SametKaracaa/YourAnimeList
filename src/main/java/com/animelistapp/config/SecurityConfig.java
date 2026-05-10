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
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }
    @Bean
    public SecurityFilterChain guvenlikFiltreZinciri(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(yetki -> yetki
                .requestMatchers("/giris", "/kayit", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/giris")                          
                .loginProcessingUrl("/giris-yap")             
                .usernameParameter("kullaniciAdi")            
                .passwordParameter("sifre")                   
                .defaultSuccessUrl("/anime/liste", true)      
                .failureUrl("/giris?hata=true")               
                .permitAll()
            )
            .logout(cikis -> cikis
                .logoutUrl("/cikis")                          
                .logoutSuccessUrl("/giris?cikis=true")        
                .invalidateHttpSession(true)                  
                .deleteCookies("JSESSIONID")                  
                .permitAll()
            )
            .userDetailsService(customUserDetailsService);
        return http.build();
    }
    @Bean
    public PasswordEncoder sifreEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager kimlikDogrulamaYoneticisi(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
