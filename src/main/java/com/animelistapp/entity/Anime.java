package com.animelistapp.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
@Entity
@Table(name = "animeler")
public class Anime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Anime adı boş bırakılamaz")
    @Size(max = 200, message = "Anime adı en fazla 200 karakter olabilir")
    @Column(name = "anime_adi", nullable = false, length = 200)
    private String animeAdi;
    @Size(max = 100, message = "Tür en fazla 100 karakter olabilir")
    @Column(length = 100)
    private String tur;
    @Min(value = 1900, message = "Yıl 1900'den küçük olamaz")
    @Max(value = 2100, message = "Yıl 2100'den büyük olamaz")
    @Column(name = "yayin_yili")
    private Integer yayinYili;
    @Size(max = 100, message = "Stüdyo adı en fazla 100 karakter olabilir")
    @Column(length = 100)
    private String studyo;
    @Min(value = 1, message = "Toplam bölüm en az 1 olmalıdır")
    @Max(value = 10000, message = "Toplam bölüm en fazla 10000 olabilir")
    @Column(name = "toplam_bolum")
    private Integer toplamBolum;
    @Lob
    @Column(name = "kapak_gorseli", columnDefinition = "LONGBLOB")
    private byte[] kapakGorseli;
    @Size(max = 50)
    @Column(name = "gorsel_tipi", length = 50)
    private String gorselTipi;
    @OneToMany(
            mappedBy = "anime",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<UserAnimeList> kullaniciListeleri = new ArrayList<>();
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
    @Transient
    public String getKapakGorseliBase64() {
        if (this.kapakGorseli == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(this.kapakGorseli);
    }
}
