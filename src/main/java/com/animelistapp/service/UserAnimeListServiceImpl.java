package com.animelistapp.service;
import com.animelistapp.entity.Anime;
import com.animelistapp.entity.IzlemeDurumu;
import com.animelistapp.entity.User;
import com.animelistapp.entity.UserAnimeList;
import com.animelistapp.repository.AnimeRepository;
import com.animelistapp.repository.UserAnimeListRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;
@Service
@Transactional(readOnly = true)
public class UserAnimeListServiceImpl implements UserAnimeListService {
    private final UserAnimeListRepository ualRepository;
    private final AnimeRepository animeRepository;
    public UserAnimeListServiceImpl(UserAnimeListRepository ualRepository,
                                    AnimeRepository animeRepository) {
        this.ualRepository = ualRepository;
        this.animeRepository = animeRepository;
    }
    @Override
    public List<UserAnimeList> kullanicininListesi(User kullanici, String siralama, String yon) {
        return ualRepository.findByKullaniciWithAnime(kullanici, siralamaOlustur(siralama, yon));
    }
    @Override
    public Optional<UserAnimeList> kayitGetir(Long id, User kullanici) {
        return ualRepository.findByIdAndKullanici(id, kullanici);
    }
    @Override
    @Transactional
    public UserAnimeList listeyeEkle(User kullanici, Long animeId,
                                     IzlemeDurumu durum, Integer puan, Integer izlenenBolum) {
        Anime anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Anime bulunamadı: id=" + animeId));
        if (ualRepository.existsByKullaniciAndAnime(kullanici, anime)) {
            throw new IllegalStateException(
                    "Bu anime zaten listenizde: " + anime.getAnimeAdi());
        }
        IzlemeDurumu finalDurum = durum != null ? durum : IzlemeDurumu.PLAN_TO_WATCH;
        UserAnimeList kayit = new UserAnimeList();
        kayit.setKullanici(kullanici);
        kayit.setAnime(anime);
        kayit.setIzlemeDurumu(finalDurum);
        kayit.setPuan(puan);
        kayit.setIzlenenBolum(bolumNormalize(finalDurum, izlenenBolum, anime.getToplamBolum()));
        return ualRepository.save(kayit);
    }
    @Override
    @Transactional
    public UserAnimeList kaydiGuncelle(Long ualId, User kullanici,
                                       IzlemeDurumu durum, Integer puan, Integer izlenenBolum) {
        UserAnimeList kayit = ualRepository.findByIdAndKullanici(ualId, kullanici)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Liste kaydı bulunamadı veya size ait değil: id=" + ualId));
        if (durum != null) kayit.setIzlemeDurumu(durum);
        kayit.setPuan(puan);  
        kayit.setIzlenenBolum(
                bolumNormalize(kayit.getIzlemeDurumu(), izlenenBolum, kayit.getAnime().getToplamBolum()));
        return ualRepository.save(kayit);
    }
    private Sort siralamaOlustur(String siralama, String yon) {
        boolean isAsc = "asc".equalsIgnoreCase(yon);
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        if ("puan".equals(siralama)) {
            return Sort.by(direction, "puan");
        } else if ("yayinYili".equals(siralama)) {
            return Sort.by(direction, "anime.yayinYili");
        } else if ("animeAdi".equals(siralama)) {
            return Sort.by(direction, "anime.animeAdi");
        }
        return Sort.by(direction, "eklenmeTarihi");
    }
    private Integer bolumNormalize(IzlemeDurumu durum, Integer izlenen, Integer toplam) {
        if (durum == IzlemeDurumu.COMPLETED && toplam != null) {
            return toplam;
        }
        if (izlenen == null) return null;
        if (izlenen < 0) return 0;
        if (toplam != null && izlenen > toplam) return toplam;
        return izlenen;
    }
    @Override
    @Transactional
    public void listedenSil(Long ualId, User kullanici) {
        long silinen = ualRepository.deleteByIdAndKullanici(ualId, kullanici);
        if (silinen == 0) {
            throw new EntityNotFoundException(
                    "Silinecek kayıt bulunamadı veya size ait değil: id=" + ualId);
        }
    }
    @Override
    public List<UserAnimeList> ara(User kullanici,
                                   String isim, String tur, String studyo,
                                   Integer yil, IzlemeDurumu durum, String siralama, String yon) {
        return ualRepository.ara(kullanici, isim, tur, studyo, yil, durum, siralamaOlustur(siralama, yon));
    }
    @Override
    public long toplamSayi(User kullanici) {
        return ualRepository.countByKullanici(kullanici);
    }
    @Override
    public long durumSayisi(User kullanici, IzlemeDurumu durum) {
        return ualRepository.countByKullaniciAndIzlemeDurumu(kullanici, durum);
    }
    @Override
    public double ortalamaPuan(User kullanici) {
        Double ort = ualRepository.ortalamaPuan(kullanici);
        return ort != null ? ort : 0.0;
    }
}
