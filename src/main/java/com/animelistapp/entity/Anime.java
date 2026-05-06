package com.animelistapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Bir anime ESERİNİ temsil eden katalog entity'si.
 *
 * Bu sınıf USER'a bağlı DEĞİLDİR — sistemdeki tüm kullanıcılar tarafından
 * paylaşılan ortak bir anime kataloğudur. Kullanıcıya özel veriler
 * (izleme durumu, kişisel puan vb.) UserAnimeList join entity'sinde tutulur.
 *
 * İlişki:
 *   Anime (1) ----< (N) UserAnimeList
 */
@Entity
@Table(name = "animeler")
public class Anime {

    /** Birincil anahtar. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Animenin adı. */
    @NotBlank(message = "Anime adı boş bırakılamaz")
    @Size(max = 200, message = "Anime adı en fazla 200 karakter olabilir")
    @Column(name = "anime_adi", nullable = false, length = 200)
    private String animeAdi;

    /** Tür(ler). Örn: "Aksiyon, Macera". */
    @Size(max = 100, message = "Tür en fazla 100 karakter olabilir")
    @Column(length = 100)
    private String tur;

    /** Yayınlanma yılı (mantıklı bir aralıkta sınırlandırılmıştır). */
    @Min(value = 1900, message = "Yıl 1900'den küçük olamaz")
    @Max(value = 2100, message = "Yıl 2100'den büyük olamaz")
    @Column(name = "yayin_yili")
    private Integer yayinYili;

    /** Yapım stüdyosu. */
    @Size(max = 100, message = "Stüdyo adı en fazla 100 karakter olabilir")
    @Column(length = 100)
    private String studyo;

    /** Toplam bölüm sayısı — Jikan'dan otomatik dolar. */
    @Min(value = 1, message = "Toplam bölüm en az 1 olmalıdır")
    @Max(value = 10000, message = "Toplam bölüm en fazla 10000 olabilir")
    @Column(name = "toplam_bolum")
    private Integer toplamBolum;

    /**
     * Kapak görseli — LONGBLOB olarak DB'de tutulur.
     * Şablonda gösterirken {@link #getKapakGorseliBase64()} kullanılır.
     */
    @Lob
    @Column(name = "kapak_gorseli", columnDefinition = "LONGBLOB")
    private byte[] kapakGorseli;

    /** Görselin MIME tipi (image/jpeg, image/png, ...). */
    @Size(max = 50)
    @Column(name = "gorsel_tipi", length = 50)
    private String gorselTipi;

    /**
     * Bu animeyi listesine eklemiş kullanıcıların kayıtları.
     * Anime silinirse bu kayıtlar da silinir.
     */
    @OneToMany(
            mappedBy = "anime",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<UserAnimeList> kullaniciListeleri = new ArrayList<>();

    // === GETTER & SETTER ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAnimeAdi() { return animeAdi; }
    public void setAnimeAdi(String animeAdi) { this.animeAdi = animeAdi; }

    public String getTur() { return tur; }
    public void setTur(String tur) { this.tur = tur; }

    public Integer getYayinYili() { return yayinYili; }
    public void setYayinYili(Integer yayinYili) { this.yayinYili = yayinYili; }

    public String getStudyo() { return studyo; }
    public void setStudyo(String studyo) { this.studyo = studyo; }

    public Integer getToplamBolum() { return toplamBolum; }
    public void setToplamBolum(Integer toplamBolum) { this.toplamBolum = toplamBolum; }

    public byte[] getKapakGorseli() { return kapakGorseli; }
    public void setKapakGorseli(byte[] kapakGorseli) { this.kapakGorseli = kapakGorseli; }

    public String getGorselTipi() { return gorselTipi; }
    public void setGorselTipi(String gorselTipi) { this.gorselTipi = gorselTipi; }

    public List<UserAnimeList> getKullaniciListeleri() { return kullaniciListeleri; }
    public void setKullaniciListeleri(List<UserAnimeList> kullaniciListeleri) {
        this.kullaniciListeleri = kullaniciListeleri;
    }

    /**
     * Thymeleaf'te statik sınıf erişimi kapalı olduğu için
     * görseli Base64 dizgisine çeviren yardımcı metot.
     * <img th:src="@{|data:${anime.gorselTipi};base64,${anime.kapakGorseliBase64}|}" />
     */
    @Transient
    public String getKapakGorseliBase64() {
        if (this.kapakGorseli == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(this.kapakGorseli);
    }
}
