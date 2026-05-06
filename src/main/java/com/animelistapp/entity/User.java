package com.animelistapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Sisteme kayıt olan kullanıcıyı temsil eden JPA entity sınıfı.
 *
 * İlişkiler:
 *   User (1) ----< (N) UserAnimeList >---- (N to 1) Anime
 *   Yani bir kullanıcının birden çok anime kayıt satırı olur,
 *   her satır bir Anime'ye bağlanır.
 *
 * Spring Security kimlik doğrulaması bu sınıftan beslenir
 * (CustomUserDetailsService -> UserRepository.findByKullaniciAdi).
 */
@Entity
@Table(
        name = "kullanicilar",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_kullanici_adi", columnNames = "kullanici_adi"),
                @UniqueConstraint(name = "uk_email", columnNames = "email")
        }
)
public class User {

    /** Birincil anahtar — DB tarafından otomatik üretilir. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Giriş için kullanılan benzersiz kullanıcı adı. */
    @NotBlank(message = "Kullanıcı adı boş bırakılamaz")
    @Size(min = 3, max = 50, message = "Kullanıcı adı 3-50 karakter arasında olmalıdır")
    @Column(name = "kullanici_adi", nullable = false, unique = true, length = 50)
    private String kullaniciAdi;

    /**
     * BCrypt ile şifrelenmiş hali saklanır.
     * NOT: Form girdisi için 6+ karakter şartı, DB'de hash uzunluğunun yetmesi için
     * sütun length değeri 100 verildi (BCrypt hash ~60 karakter).
     */
    @NotBlank(message = "Şifre boş bırakılamaz")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    @Column(nullable = false, length = 100)
    private String sifre;

    /** Benzersiz e-posta adresi. */
    @NotBlank(message = "E-posta boş bırakılamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 100, message = "E-posta en fazla 100 karakter olabilir")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /** Spring Security rol etiketi. Varsayılan ROLE_USER. */
    @Column(nullable = false, length = 20)
    private String rol = "ROLE_USER";

    /**
     * Kullanıcının izleme listesi satırları (User -> UserAnimeList).
     *
     * mappedBy = "kullanici"  -> ilişkinin sahibi UserAnimeList tarafıdır.
     * cascade = ALL           -> kullanıcı silinirse listeleri de silinir.
     * orphanRemoval = true    -> listeden çıkarılan satır DB'den de silinir.
     * fetch = LAZY            -> performans için ihtiyaç duyulunca yüklenir.
     */
    @OneToMany(
            mappedBy = "kullanici",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<UserAnimeList> animeListesi = new ArrayList<>();

    // === GETTER & SETTER ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKullaniciAdi() { return kullaniciAdi; }
    public void setKullaniciAdi(String kullaniciAdi) { this.kullaniciAdi = kullaniciAdi; }

    public String getSifre() { return sifre; }
    public void setSifre(String sifre) { this.sifre = sifre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public List<UserAnimeList> getAnimeListesi() { return animeListesi; }
    public void setAnimeListesi(List<UserAnimeList> animeListesi) { this.animeListesi = animeListesi; }
}
