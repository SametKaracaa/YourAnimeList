package com.animelistapp.service;

import com.animelistapp.entity.IzlemeDurumu;
import com.animelistapp.entity.User;
import com.animelistapp.entity.UserAnimeList;

import java.util.List;
import java.util.Optional;

/**
 * Kullanıcının izleme listesi (UserAnimeList) iş kuralları.
 *
 * Bütün metotlar User parametresi alır → asla başka bir kullanıcının
 * kaydı dönmez veya değiştirilmez (yetkisiz erişimi servis seviyesinde keser).
 */
public interface UserAnimeListService {

    /** Kullanıcının izleme listesinin tamamı (anime detaylarıyla). */
    List<UserAnimeList> kullanicininListesi(User kullanici);

    /** Belirli bir kayıt — yalnızca sahibi olan kullanıcı erişebilir. */
    Optional<UserAnimeList> kayitGetir(Long id, User kullanici);

    /**
     * Listeye yeni anime ekler.
     *
     * @throws IllegalStateException kullanıcı bu animeyi zaten listesine eklediyse
     */
    UserAnimeList listeyeEkle(User kullanici, Long animeId,
                              IzlemeDurumu durum, Integer puan, Integer izlenenBolum);

    /** Bir liste kaydını günceller (durum + puan + izlenen bölüm). */
    UserAnimeList kaydiGuncelle(Long ualId, User kullanici,
                                IzlemeDurumu durum, Integer puan, Integer izlenenBolum);

    /** Yalnızca sahibi siler; aksi halde IllegalStateException. */
    void listedenSil(Long ualId, User kullanici);

    /**
     * Dinamik arama — 5 boyutlu (isim + tür + stüdyo + yıl + durum).
     * Boş gelen parametre o kriteri yok sayar.
     */
    List<UserAnimeList> ara(User kullanici,
                            String isim, String tur, String studyo,
                            Integer yil, IzlemeDurumu durum);

    // === İSTATİSTİK ===
    long toplamSayi(User kullanici);
    long durumSayisi(User kullanici, IzlemeDurumu durum);
    double ortalamaPuan(User kullanici);
}
