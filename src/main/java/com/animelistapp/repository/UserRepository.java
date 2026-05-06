package com.animelistapp.repository;

import com.animelistapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Kullanıcı tablosuna (kullanicilar) erişim sağlayan Spring Data repository.
 *
 * JpaRepository<User, Long> -> Long kimlikli User entity'si için CRUD,
 * sıralama ve sayfalama desteğini hazır olarak sunar.
 *
 * Aşağıdaki metotlar Spring Data JPA'nın "method-name query derivation"
 * (metot adından sorgu üretme) özelliğini kullanır — implementasyon
 * runtime'da otomatik üretilir.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Kullanıcı adına göre tek kullanıcı bulur.
     * Spring Security'nin CustomUserDetailsService'i tarafından
     * kullanıcının giriş bilgilerini okumak için çağrılır.
     */
    Optional<User> findByKullaniciAdi(String kullaniciAdi);

    /**
     * E-posta üzerinden kullanıcı arar (örn. "şifre sıfırla" akışı için).
     */
    Optional<User> findByEmail(String email);

    /**
     * Kayıt formundaki "kullanıcı adı zaten alınmış mı?" kontrolünde kullanılır.
     * Bütün User'ı çekmek yerine sadece COUNT yapar -> daha hafif sorgu.
     */
    boolean existsByKullaniciAdi(String kullaniciAdi);

    /**
     * Kayıt formundaki "bu e-posta zaten kayıtlı mı?" kontrolü.
     */
    boolean existsByEmail(String email);
}
