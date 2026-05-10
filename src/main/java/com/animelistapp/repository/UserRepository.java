package com.animelistapp.repository;
import com.animelistapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKullaniciAdi(String kullaniciAdi);
    Optional<User> findByEmail(String email);
    boolean existsByKullaniciAdi(String kullaniciAdi);
    boolean existsByEmail(String email);
}
