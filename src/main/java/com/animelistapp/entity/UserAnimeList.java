package com.animelistapp.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
@Entity
@Table(
        name = "kullanici_anime_listesi",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_kullanici_anime",
                columnNames = {"kullanici_id", "anime_id"}
        )
)
public class UserAnimeList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kullanici_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ual_kullanici"))
    private User kullanici;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "anime_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ual_anime"))
    private Anime anime;
    @NotNull(message = "İzleme durumu seçilmelidir")
    @Enumerated(EnumType.STRING)
    @Column(name = "izleme_durumu", nullable = false, length = 20)
    private IzlemeDurumu izlemeDurumu = IzlemeDurumu.PLAN_TO_WATCH;
    @Min(value = 1, message = "Puan en az 1 olmalıdır")
    @Max(value = 10, message = "Puan en fazla 10 olabilir")
    @Column
    private Integer puan;
    @Min(value = 0, message = "İzlenen bölüm negatif olamaz")
    @Max(value = 10000, message = "İzlenen bölüm en fazla 10000 olabilir")
    @Column(name = "izlenen_bolum")
    private Integer izlenenBolum;
    @Column(name = "eklenme_tarihi", nullable = false, updatable = false)
    private LocalDateTime eklenmeTarihi;
    @PrePersist
    protected void onCreate() {
        if (this.eklenmeTarihi == null) {
            this.eklenmeTarihi = LocalDateTime.now();
        }
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getKullanici() { return kullanici; }
    public void setKullanici(User kullanici) { this.kullanici = kullanici; }
    public Anime getAnime() { return anime; }
    public void setAnime(Anime anime) { this.anime = anime; }
    public IzlemeDurumu getIzlemeDurumu() { return izlemeDurumu; }
    public void setIzlemeDurumu(IzlemeDurumu izlemeDurumu) { this.izlemeDurumu = izlemeDurumu; }
    public Integer getPuan() { return puan; }
    public void setPuan(Integer puan) { this.puan = puan; }
    public Integer getIzlenenBolum() { return izlenenBolum; }
    public void setIzlenenBolum(Integer izlenenBolum) { this.izlenenBolum = izlenenBolum; }
    public LocalDateTime getEklenmeTarihi() { return eklenmeTarihi; }
    public void setEklenmeTarihi(LocalDateTime eklenmeTarihi) { this.eklenmeTarihi = eklenmeTarihi; }
}
