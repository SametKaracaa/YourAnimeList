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
        List<SimpleGrantedAuthority> yetkiler = List.of(
                new SimpleGrantedAuthority(user.getRol())
        );
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getKullaniciAdi())
                .password(user.getSifre())            
                .authorities(yetkiler)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
