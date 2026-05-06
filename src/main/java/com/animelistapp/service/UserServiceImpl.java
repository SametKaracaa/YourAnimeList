package com.animelistapp.service;

import com.animelistapp.entity.User;
import com.animelistapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * UserService implementasyonu.
 *
 * - Şifreler BCrypt ile hashlenir (PasswordEncoder DI yoluyla SecurityConfig'den gelir).
 * - Yazma operasyonları @Transactional ile sarılır; okuma operasyonları
 *   @Transactional(readOnly = true) ile işaretlenir (Hibernate optimizasyonu).
 */
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Kayıt akışı:
     *   1) Çakışma kontrolleri (kullanıcı adı + e-posta unique).
     *   2) Şifreyi BCrypt ile hashle.
     *   3) Rol ata (varsayılan ROLE_USER).
     *   4) DB'ye kaydet.
     *
     * Çakışma durumunda IllegalArgumentException; controller bu hatayı yakalayıp
     * forma "kullanıcı adı zaten alınmış" gibi binding error olarak yansıtır.
     */
    @Override
    @Transactional
    public User kullaniciKaydet(User user) {

        if (userRepository.existsByKullaniciAdi(user.getKullaniciAdi())) {
            throw new IllegalArgumentException("Bu kullanıcı adı zaten alınmış.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kayıtlı.");
        }

        // BCrypt ile şifrele — DB'ye asla düz metin şifre yazılmaz
        user.setSifre(passwordEncoder.encode(user.getSifre()));

        // Güvenlik: form'dan rol gelmiş olsa bile zorla ROLE_USER yap
        user.setRol("ROLE_USER");

        return userRepository.save(user);
    }

    @Override
    public Optional<User> kullaniciAdiylaGetir(String kullaniciAdi) {
        return userRepository.findByKullaniciAdi(kullaniciAdi);
    }

    @Override
    public Optional<User> emailIleGetir(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean kullaniciAdiMevcutMu(String kullaniciAdi) {
        return userRepository.existsByKullaniciAdi(kullaniciAdi);
    }

    @Override
    public boolean emailMevcutMu(String email) {
        return userRepository.existsByEmail(email);
    }
}
