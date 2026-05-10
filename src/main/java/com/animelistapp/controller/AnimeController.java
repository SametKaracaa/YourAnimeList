package com.animelistapp.controller;

import com.animelistapp.entity.Anime;
import com.animelistapp.entity.IzlemeDurumu;
import com.animelistapp.entity.User;
import com.animelistapp.entity.UserAnimeList;
import com.animelistapp.service.AnimeService;
import com.animelistapp.service.UserAnimeListService;
import com.animelistapp.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Kullanıcının kendi izleme listesi üzerindeki tüm CRUD + arama akışlarını yönetir.
 *
 * Bu controller iki entity'i bir arada düzenler: Anime (katalog) + UserAnimeList (ilişki).
 *   - Yeni satır ekleme: önce katalog Anime'si yaratılır, sonra UAL bağlanır.
 *   - Düzenleme: hem Anime hem UAL güncellenir.
 *   - Silme: yalnızca UAL satırı silinir; Anime kataloğu kalır.
 *
 * URL eşlemeleri (mevcut şablon URL'leriyle uyumlu):
 *   GET  /anime/liste                 -> liste + arama
 *   GET  /anime/ekle                  -> yeni form
 *   GET  /anime/duzenle/{ualId}       -> düzenleme formu
 *   POST /anime/kaydet                -> kayıt (yeni VEYA güncelleme)
 *   GET  /anime/sil/{ualId}           -> sil
 *   GET  /anime/gorsel/{animeId}      -> BLOB kapak görselini bytes olarak döndürür
 */
@Controller
@RequestMapping("/anime")
public class AnimeController {

    private final AnimeService animeService;
    private final UserAnimeListService ualService;
    private final UserService userService;

    public AnimeController(AnimeService animeService,
                           UserAnimeListService ualService,
                           UserService userService) {
        this.animeService = animeService;
        this.ualService = ualService;
        this.userService = userService;
    }

    // ============================================================
    // LİSTE + ARAMA
    // ============================================================

    /**
     * Kullanıcının izleme listesi + dinamik arama.
     *
     * 5 opsiyonel filtre: isim, tür, stüdyo, yıl, durum.
     * Hepsi boşsa tüm liste; en az biri doluysa filtreli sorgu çalışır.
     */
    @GetMapping("/liste")
    public String liste(@RequestParam(required = false) String isim,
                        @RequestParam(required = false) String tur,
                        @RequestParam(required = false) String studyo,
                        @RequestParam(required = false) String yilStr,
                        @RequestParam(required = false) String durum,
                        @RequestParam(required = false) String siralama,
                        @RequestParam(required = false, defaultValue = "desc") String yon,
                        Authentication authentication,
                        Model model) {

        User user = oturumKullanicisi(authentication);

        // Form'dan boş string gelebilir; tip dönüşümünü manuel yapıyoruz.
        // (Spring StringToEnumConverterFactory boş string'i enum'a çeviremez)
        Integer yil           = yilParseEt(yilStr);
        IzlemeDurumu durumEnum = durumParseEt(durum);
        String temizIsim      = bosluksuzVeyaNull(isim);
        String temizTur       = bosluksuzVeyaNull(tur);
        String temizStudyo    = bosluksuzVeyaNull(studyo);

        boolean filtreVarMi = temizIsim != null || temizTur != null
                            || temizStudyo != null || yil != null || durumEnum != null;

        java.util.List<UserAnimeList> liste = filtreVarMi
                ? ualService.ara(user, temizIsim, temizTur, temizStudyo, yil, durumEnum, siralama, yon)
                : ualService.kullanicininListesi(user, siralama, yon);

        // İstatistik kartları
        long toplam      = ualService.toplamSayi(user);
        long izleniyor   = ualService.durumSayisi(user, IzlemeDurumu.WATCHING);
        long tamamlanan  = ualService.durumSayisi(user, IzlemeDurumu.COMPLETED);
        long planlanan   = ualService.durumSayisi(user, IzlemeDurumu.PLAN_TO_WATCH);
        double ortalama  = ualService.ortalamaPuan(user);

        model.addAttribute("liste", liste);
        model.addAttribute("kullaniciAdi", user.getKullaniciAdi());
        model.addAttribute("toplamAnime",  toplam);
        model.addAttribute("izleniyor",    izleniyor);
        model.addAttribute("tamamlanan",   tamamlanan);
        model.addAttribute("planlanan",    planlanan);
        model.addAttribute("ortalamaPuan", String.format("%.1f", ortalama));

        // Arama formundaki değerleri korumak için
        model.addAttribute("aramaIsim",   temizIsim);
        model.addAttribute("aramaTur",    temizTur);
        model.addAttribute("aramaStudyo", temizStudyo);
        model.addAttribute("aramaYil",    yil);
        model.addAttribute("aramaDurum",  durumEnum);
        model.addAttribute("aramaSiralama", siralama);
        model.addAttribute("aramaYon", yon);

        // Filtre dropdown'ları
        model.addAttribute("turler",         animeService.tumTurleriGetir());
        model.addAttribute("studyolar",      animeService.tumStudyolariGetir());
        model.addAttribute("yillar",         animeService.tumYillariGetir());
        model.addAttribute("izlemeDurumlari", IzlemeDurumu.values());

        return "anime-listesi";
    }

    // ============================================================
    // EKLE / DÜZENLE FORMLARI
    // ============================================================

    /** Yeni anime ekleme formu. */
    @GetMapping("/ekle")
    public String ekleFormu(Model model) {
        if (!model.containsAttribute("anime")) {
            model.addAttribute("anime", new Anime());
        }
        model.addAttribute("ualId", null);
        model.addAttribute("seciliDurum", IzlemeDurumu.PLAN_TO_WATCH);
        model.addAttribute("seciliPuan", null);
        model.addAttribute("seciliIzlenenBolum", null);
        model.addAttribute("izlemeDurumlari", IzlemeDurumu.values());
        return "anime-form";
    }

    /** Düzenleme formu — UAL satırı ve bağlı Anime ile pre-fill. */
    @GetMapping("/duzenle/{ualId}")
    public String duzenleFormu(@PathVariable Long ualId,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        User user = oturumKullanicisi(authentication);

        UserAnimeList ual = ualService.kayitGetir(ualId, user).orElse(null);
        if (ual == null) {
            redirectAttributes.addFlashAttribute("hataMesaji",
                    "Düzenlenecek kayıt bulunamadı veya size ait değil.");
            return "redirect:/anime/liste";
        }

        model.addAttribute("anime", ual.getAnime());
        model.addAttribute("ualId", ual.getId());
        model.addAttribute("seciliDurum", ual.getIzlemeDurumu());
        model.addAttribute("seciliPuan", ual.getPuan());
        model.addAttribute("seciliIzlenenBolum", ual.getIzlenenBolum());
        model.addAttribute("izlemeDurumlari", IzlemeDurumu.values());
        return "anime-form";
    }

    // ============================================================
    // KAYIT (yeni VEYA güncelleme)
    // ============================================================

    /**
     * Tek bir endpoint hem yeni kayıt hem de güncelleme yapar:
     *   - ualId boşsa  -> YENİ ekleme: katalog Anime + UAL oluşturulur
     *   - ualId doluysa -> GÜNCELLEME: katalog Anime alanları ve UAL'in durum/puan değerleri güncellenir
     *
     * BLOB görsel: form'daki "kapakDosyasi" alanı AnimeService'in
     * private kapakGorseliniUygula() metodu tarafından işlenir.
     */
    @PostMapping("/kaydet")
    public String kaydet(@Valid @ModelAttribute("anime") Anime anime,
                         BindingResult bindingResult,
                         @RequestParam(value = "ualId", required = false) Long ualId,
                         @RequestParam(value = "izlemeDurumu") IzlemeDurumu izlemeDurumu,
                         @RequestParam(value = "puan", required = false) Integer puan,
                         @RequestParam(value = "izlenenBolum", required = false) Integer izlenenBolum,
                         @RequestParam(value = "kapakDosyasi", required = false) MultipartFile kapakDosyasi,
                         @RequestParam(value = "gorselUrl", required = false) String gorselUrl,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        // 1) Anime entity validation
        if (bindingResult.hasErrors()) {
            geriDolduFormu(model, ualId, izlemeDurumu, puan, izlenenBolum);
            return "anime-form";
        }

        User user = oturumKullanicisi(authentication);

        try {
            if (ualId == null) {
                // === YENİ EKLEME === (Jikan'dan geldiyse gorselUrl dolu olur)
                Anime kaydedilen = animeService.animeKaydetUrlGorselle(anime, kapakDosyasi, gorselUrl);
                ualService.listeyeEkle(user, kaydedilen.getId(), izlemeDurumu, puan, izlenenBolum);
                redirectAttributes.addFlashAttribute("basariMesaji",
                        "\"" + kaydedilen.getAnimeAdi() + "\" listenize eklendi.");
            } else {
                // === GÜNCELLEME ===
                UserAnimeList mevcut = ualService.kayitGetir(ualId, user)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Liste kaydı bulunamadı: id=" + ualId));

                Anime hedefAnime = mevcut.getAnime();
                hedefAnime.setAnimeAdi(anime.getAnimeAdi());
                hedefAnime.setTur(anime.getTur());
                hedefAnime.setYayinYili(anime.getYayinYili());
                hedefAnime.setStudyo(anime.getStudyo());
                hedefAnime.setToplamBolum(anime.getToplamBolum());
                animeService.animeKaydetUrlGorselle(hedefAnime, kapakDosyasi, gorselUrl);

                ualService.kaydiGuncelle(ualId, user, izlemeDurumu, puan, izlenenBolum);

                redirectAttributes.addFlashAttribute("basariMesaji",
                        "Liste kaydınız güncellendi.");
            }

        } catch (IllegalArgumentException ex) {
            // BLOB / MIME hataları (kapakGorseliniUygula'dan)
            bindingResult.reject("anime.image.error", ex.getMessage());
            geriDolduFormu(model, ualId, izlemeDurumu, puan, izlenenBolum);
            return "anime-form";

        } catch (IllegalStateException ex) {
            // "Bu anime zaten listenizde" gibi durumlar
            redirectAttributes.addFlashAttribute("hataMesaji", ex.getMessage());
            return "redirect:/anime/liste";

        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("hataMesaji",
                    "Görsel işlenirken bir hata oluştu: " + ex.getMessage());
            return "redirect:/anime/liste";
        }

        return "redirect:/anime/liste";
    }

    // ============================================================
    // SİLME
    // ============================================================

    /**
     * Listeden silme. Anime kataloğu kaydı SİLİNMEZ — başka kullanıcılar
     * hâlâ aynı animeyi listelerinde tutuyor olabilir.
     *
     * Not: Üretimde POST + CSRF tercih edilmeli. Eğitsel basitlik için GET kullanıldı.
     */
    @GetMapping("/sil/{ualId}")
    public String sil(@PathVariable Long ualId,
                      Authentication authentication,
                      RedirectAttributes redirectAttributes) {

        User user = oturumKullanicisi(authentication);
        try {
            ualService.listedenSil(ualId, user);
            redirectAttributes.addFlashAttribute("basariMesaji", "Kayıt silindi.");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("hataMesaji", ex.getMessage());
        }
        return "redirect:/anime/liste";
    }

    // ============================================================
    // BLOB GÖRSELİ DOĞRUDAN STREAM ETME
    // ============================================================

    /**
     * Anime kapağını ham bytes olarak döndüren endpoint.
     * Şablonda <img th:src="@{/anime/gorsel/{id}(id=${anime.id})}" /> şeklinde kullanılabilir.
     *
     * Avantaj: Base64 ile inline gömmek yerine HTTP üzerinden cache'lenebilir bir kaynak olur.
     * Dezavantaj: Her görsel için ayrı bir HTTP isteği — küçük listeler için sorun yok.
     */
    @GetMapping("/gorsel/{animeId}")
    public ResponseEntity<byte[]> gorsel(@PathVariable Long animeId) {

        Anime anime = animeService.animeGetir(animeId).orElse(null);
        if (anime == null || anime.getKapakGorseli() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(
                    anime.getGorselTipi() != null ? anime.getGorselTipi() : "image/jpeg");
        } catch (Exception ex) {
            mediaType = MediaType.IMAGE_JPEG;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(anime.getKapakGorseli());
    }

    // ============================================================
    // YARDIMCI METOTLAR
    // ============================================================

    /** Spring Security oturumundaki kullanıcı adından DB'deki User'ı getirir. */
    private User oturumKullanicisi(Authentication authentication) {
        String kullaniciAdi = authentication.getName();
        return userService.kullaniciAdiylaGetir(kullaniciAdi)
                .orElseThrow(() -> new IllegalStateException(
                        "Oturumdaki kullanıcı veritabanında yok: " + kullaniciAdi));
    }

    /** Boş veya yalnızca whitespace olan string'i null'a çevirir. */
    private String bosluksuzVeyaNull(String s) {
        return (s != null && !s.trim().isEmpty()) ? s.trim() : null;
    }

    /** Form'dan gelen yıl string'ini güvenli şekilde Integer'a çevirir. */
    private Integer yilParseEt(String yilStr) {
        if (yilStr == null || yilStr.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(yilStr.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** Form'dan gelen durum string'ini güvenli şekilde IzlemeDurumu'na çevirir. */
    private IzlemeDurumu durumParseEt(String durumStr) {
        if (durumStr == null || durumStr.trim().isEmpty()) return null;
        try {
            return IzlemeDurumu.valueOf(durumStr.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** Validation hatasında formu yeniden açarken state'i model'e geri koyar. */
    private void geriDolduFormu(Model model, Long ualId,
                                IzlemeDurumu izlemeDurumu, Integer puan, Integer izlenenBolum) {
        model.addAttribute("ualId", ualId);
        model.addAttribute("seciliDurum", izlemeDurumu);
        model.addAttribute("seciliPuan", puan);
        model.addAttribute("seciliIzlenenBolum", izlenenBolum);
        model.addAttribute("izlemeDurumlari", IzlemeDurumu.values());
    }
}
