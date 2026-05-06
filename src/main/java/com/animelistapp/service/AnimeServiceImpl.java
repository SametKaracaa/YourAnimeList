package com.animelistapp.service;

import com.animelistapp.entity.Anime;
import com.animelistapp.repository.AnimeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * AnimeService implementasyonu — katalog işlemleri ve BLOB kapak görseli yönetimi.
 *
 * BLOB iş akışı:
 *   - Form'dan {@link MultipartFile} olarak gelen görsel byte dizisine çevrilir.
 *   - Çift yönlü doğrulama: dosya null/boş değil; MIME tipi "image/*" ile başlıyor;
 *     boyut multipart konfigürasyonu (5MB) tarafından zaten sınırlı.
 *   - Görsel byte[] olarak {@code anime.kapakGorseli}'ne, MIME tipi
 *     {@code anime.gorselTipi}'ne yazılır.
 *   - Boş dosya gelirse mevcut görsel KORUNUR (update sırasında kullanıcı
 *     resmi değiştirmek istemediği durumlar için).
 */
@Service
@Transactional(readOnly = true)
public class AnimeServiceImpl implements AnimeService {

    /** İzin verilen MIME tipleri — başka tip dosya yüklenemez. */
    private static final Set<String> KABUL_EDILEN_MIME = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    /** Defansif limit: properties'teki 5MB ile uyumlu. */
    private static final long MAKS_BOYUT_BYTE = 5L * 1024 * 1024;

    private final AnimeRepository animeRepository;

    public AnimeServiceImpl(AnimeRepository animeRepository) {
        this.animeRepository = animeRepository;
    }

    // ============================================================
    // CRUD
    // ============================================================

    @Override
    @Transactional
    public Anime animeKaydet(Anime anime, MultipartFile kapakDosyasi) throws IOException {
        kapakGorseliniUygula(anime, kapakDosyasi);
        return animeRepository.save(anime);
    }

    @Override
    @Transactional
    public Anime animeKaydetUrlGorselle(Anime anime, MultipartFile kapakDosyasi, String gorselUrl) throws IOException {
        // Önce manuel yüklenen dosyayı dene; gelmediyse URL'den indir
        if (kapakDosyasi != null && !kapakDosyasi.isEmpty()) {
            kapakGorseliniUygula(anime, kapakDosyasi);
        } else if (gorselUrl != null && !gorselUrl.isBlank()) {
            kapakGorseliniUrldenIndir(anime, gorselUrl.trim());
        }
        return animeRepository.save(anime);
    }

    @Override
    public Optional<Anime> animeGetir(Long id) {
        return animeRepository.findById(id);
    }

    @Override
    public Anime animeGetirOrThrow(Long id) {
        return animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Anime bulunamadı: id=" + id));
    }

    @Override
    public List<Anime> tumKatalog() {
        return animeRepository.findAllByOrderByAnimeAdiAsc();
    }

    @Override
    @Transactional
    public void animeSil(Long id) {
        // Var olmayan id ile silmeye çalışmak EmptyResultDataAccessException atar;
        // önce kontrol et.
        if (!animeRepository.existsById(id)) {
            throw new EntityNotFoundException("Silinecek anime bulunamadı: id=" + id);
        }
        animeRepository.deleteById(id);
        // OneToMany cascade=ALL + orphanRemoval=true sayesinde
        // bu animeye bağlı tüm UserAnimeList kayıtları da silinecek.
    }

    // ============================================================
    // ARAMA / FİLTRE
    // ============================================================

    @Override
    public List<Anime> kataloguAra(String isim, String tur, String studyo, Integer yil) {
        return animeRepository.ara(isim, tur, studyo, yil);
    }

    @Override
    public List<String> tumTurleriGetir() {
        return animeRepository.findDistinctTurler();
    }

    @Override
    public List<String> tumStudyolariGetir() {
        return animeRepository.findDistinctStudyolar();
    }

    @Override
    public List<Integer> tumYillariGetir() {
        return animeRepository.findDistinctYillar();
    }

    // ============================================================
    // BLOB KAPAK GÖRSELİ — özel yardımcı
    // ============================================================

    /**
     * Form'dan gelen MultipartFile'ı byte[] olarak Anime'ye yazar.
     *
     *  - Dosya gelmediyse (null veya isEmpty): hiçbir şey yapma → mevcut görsel korunur.
     *  - MIME tipi "image/jpeg|png|gif|webp" değilse: IllegalArgumentException.
     *  - Boyut 5 MB üstüyse: IllegalArgumentException.
     *  - Geçerliyse: byte[] + MIME tipi entity'ye basılır.
     *
     * NOT: file.getBytes() RAM'e komple yükler. Çok büyük dosyalar için
     * stream tabanlı yaklaşım gerekirdi; ama kapak görseli senaryosunda
     * dosyalar ufak (<5MB) olduğu için bu yaklaşım yeterli.
     */
    private void kapakGorseliniUygula(Anime anime, MultipartFile dosya) throws IOException {

        // Yeni dosya yok → mevcut görseli koru
        if (dosya == null || dosya.isEmpty()) {
            return;
        }

        // Boyut kontrolü (defansif — multipart config zaten sınırlıyor)
        if (dosya.getSize() > MAKS_BOYUT_BYTE) {
            throw new IllegalArgumentException(
                    "Görsel 5 MB'tan büyük olamaz. Yüklenen: "
                            + (dosya.getSize() / 1024) + " KB");
        }

        // MIME tipi kontrolü
        String mimeTipi = dosya.getContentType();
        if (mimeTipi == null || !KABUL_EDILEN_MIME.contains(mimeTipi.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Yalnızca JPG, PNG, GIF veya WEBP yüklenebilir. Gelen: " + mimeTipi);
        }

        // Bytes'ı oku ve entity'ye yaz
        anime.setKapakGorseli(dosya.getBytes());
        anime.setGorselTipi(mimeTipi);
    }

    /**
     * Jikan API'den gelen kapak URL'ini arka planda indirip BLOB olarak kaydeder.
     * Yalnızca http(s) izinli; SSRF'i azaltmak için diğer şemalar reddedilir.
     */
    private void kapakGorseliniUrldenIndir(Anime anime, String gorselUrl) throws IOException {
        URL url;
        try {
            url = URI.create(gorselUrl).toURL();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Geçersiz görsel URL'si: " + gorselUrl);
        }
        String protocol = url.getProtocol();
        if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
            throw new IllegalArgumentException("Yalnızca http/https şeması desteklenir: " + protocol);
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("User-Agent", "AnimeListApp/1.0");
        conn.setInstanceFollowRedirects(true);

        try (InputStream in = conn.getInputStream()) {
            String mimeTipi = conn.getContentType();
            if (mimeTipi == null) {
                mimeTipi = "image/jpeg";
            } else {
                int semicolon = mimeTipi.indexOf(';');
                if (semicolon > -1) mimeTipi = mimeTipi.substring(0, semicolon).trim();
                mimeTipi = mimeTipi.toLowerCase();
            }
            if (!KABUL_EDILEN_MIME.contains(mimeTipi)) {
                throw new IllegalArgumentException(
                        "Uzak görsel desteklenmeyen MIME tipinde: " + mimeTipi);
            }

            byte[] bytes = in.readAllBytes();
            if (bytes.length == 0) {
                throw new IllegalArgumentException("Uzak görsel boş döndü.");
            }
            if (bytes.length > MAKS_BOYUT_BYTE) {
                throw new IllegalArgumentException(
                        "Uzak görsel 5 MB'tan büyük: " + (bytes.length / 1024) + " KB");
            }

            anime.setKapakGorseli(bytes);
            anime.setGorselTipi(mimeTipi);
        } finally {
            conn.disconnect();
        }
    }
}
