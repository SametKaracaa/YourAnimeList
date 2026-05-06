package com.animelistapp.service;

import com.animelistapp.entity.Anime;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Anime KATALOĞU üzerindeki işlemleri tanımlar.
 * Bu seviye user'dan bağımsızdır — kullanıcı bazlı işlemler için
 * {@link UserAnimeListService} kullanılmalıdır.
 */
public interface AnimeService {

    /**
     * Yeni anime ekler veya mevcut animeyi günceller.
     *
     * @param anime         kaydedilecek katalog kaydı
     * @param kapakDosyasi  opsiyonel kapak görseli; null/boş ise mevcut görsel korunur
     * @return persist sonrası anime (id atanmış)
     * @throws IOException görsel okunamazsa
     * @throws IllegalArgumentException görsel image/* dışında bir tipte ise
     */
    Anime animeKaydet(Anime anime, MultipartFile kapakDosyasi) throws IOException;

    /**
     * Yeni: dosya gelmediyse Jikan'dan gelen URL'i arka planda indirip kaydeder.
     * Öncelik dosyada; ikisi de boşsa görsel atanmaz.
     */
    Anime animeKaydetUrlGorselle(Anime anime, MultipartFile kapakDosyasi, String gorselUrl) throws IOException;

    Optional<Anime> animeGetir(Long id);

    Anime animeGetirOrThrow(Long id);

    List<Anime> tumKatalog();

    void animeSil(Long id);

    /**
     * Dinamik arama (4 boyut: isim + tür + stüdyo + yıl).
     * Boş gelen parametre o kriterden filtreleme yapmaz.
     */
    List<Anime> kataloguAra(String isim, String tur, String studyo, Integer yil);

    /** Form filtreleri için DISTINCT türler. */
    List<String> tumTurleriGetir();

    /** Form filtreleri için DISTINCT stüdyolar. */
    List<String> tumStudyolariGetir();

    /** Form filtreleri için DISTINCT yayın yılları (en yeniden eskiye). */
    List<Integer> tumYillariGetir();
}
