package com.animelistapp.service;

import com.animelistapp.entity.User;
import com.animelistapp.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Spring Security'nin kimlik doğrulaması sırasında çağırdığı servis.
 *
 * AKIŞ:
 *   1) Kullanıcı /login sayfasında kullanıcı adı + şifre gönderir.
 *   2) Spring Security DaoAuthenticationProvider'ı,
 *      bu sınıfın {@link #loadUserByUsername(String)} metodunu çağırır.
 *   3) DB'den User entity'si bulunur.
 *   4) Spring Security'nin {@link UserDetails} arayüzüne uygun bir
 *      org.springframework.security.core.userdetails.User nesnesi döner.
 *   5) Provider, bu UserDetails'in şifresi ile form'dan gelen ham şifreyi
 *      PasswordEncoder.matches() ile karşılaştırır → BCrypt doğrulaması.
 *
 * NOT: PasswordEncoder doğrulamasını biz YAPMIYORUZ.
 *      O işi Spring Security'nin DaoAuthenticationProvider'ı, SecurityConfig'de
 *      tanımladığımız BCryptPasswordEncoder bean'ini kullanarak otomatik yapar.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String kullaniciAdi) throws UsernameNotFoundException {

        User user = userRepository.findByKullaniciAdi(kullaniciAdi)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Kullanıcı bulunamadı: " + kullaniciAdi));

        // Rol "ROLE_USER" gibi tek bir String. SimpleGrantedAuthority ile sarıyoruz.
        // İleride birden fazla rol olursa (ROLE_ADMIN, ROLE_MOD vb.) liste genişletilir.
        List<SimpleGrantedAuthority> yetkiler = List.of(
                new SimpleGrantedAuthority(user.getRol())
        );

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getKullaniciAdi())
                .password(user.getSifre())            // BCrypt hash — Spring Security karşılaştıracak
                .authorities(yetkiler)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
