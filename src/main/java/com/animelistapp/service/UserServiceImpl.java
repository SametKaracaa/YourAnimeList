package com.animelistapp.service;
import com.animelistapp.entity.User;
import com.animelistapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    @Transactional
    public User kullaniciKaydet(User user) {
        if ("admin".equalsIgnoreCase(user.getKullaniciAdi())) {
            throw new IllegalArgumentException("Bu kullanıcı adı kullanılamaz.");
        }
        if (userRepository.existsByKullaniciAdi(user.getKullaniciAdi())) {
            throw new IllegalArgumentException("Bu kullanıcı adı zaten alınmış.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kayıtlı.");
        }
        user.setSifre(passwordEncoder.encode(user.getSifre()));
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
    @Override
    public List<User> tumKullanicilariGetir() {
        return userRepository.findAll();
    }
    @Override
    @Transactional
    public void kullaniciSil(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Silinecek kullanıcı bulunamadı: id=" + id);
        }
        userRepository.deleteById(id);
    }
    @Override
    @Transactional
    public void rolGuncelle(Long id, String yeniRol) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Kullanıcı bulunamadı: id=" + id));
        user.setRol(yeniRol);
        userRepository.save(user);
    }
}
