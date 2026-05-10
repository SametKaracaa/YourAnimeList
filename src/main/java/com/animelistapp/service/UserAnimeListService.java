package com.animelistapp.service;
import com.animelistapp.entity.IzlemeDurumu;
import com.animelistapp.entity.User;
import com.animelistapp.entity.UserAnimeList;
import java.util.List;
import java.util.Optional;
public interface UserAnimeListService {
    List<UserAnimeList> kullanicininListesi(User kullanici, String siralama, String yon);
    Optional<UserAnimeList> kayitGetir(Long id, User kullanici);
    UserAnimeList listeyeEkle(User kullanici, Long animeId,
                              IzlemeDurumu durum, Integer puan, Integer izlenenBolum);
    UserAnimeList kaydiGuncelle(Long ualId, User kullanici,
                                IzlemeDurumu durum, Integer puan, Integer izlenenBolum);
    void listedenSil(Long ualId, User kullanici);
    List<UserAnimeList> ara(User kullanici,
                            String isim, String tur, String studyo,
                            Integer yil, IzlemeDurumu durum, String siralama, String yon);
    long toplamSayi(User kullanici);
    long durumSayisi(User kullanici, IzlemeDurumu durum);
    double ortalamaPuan(User kullanici);
}
