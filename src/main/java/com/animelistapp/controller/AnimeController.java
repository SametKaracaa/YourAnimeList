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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
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
        model.addAttribute("aramaIsim",   temizIsim);
        model.addAttribute("aramaTur",    temizTur);
        model.addAttribute("aramaStudyo", temizStudyo);
        model.addAttribute("aramaYil",    yil);
        model.addAttribute("aramaDurum",  durumEnum);
        model.addAttribute("aramaSiralama", siralama);
        model.addAttribute("aramaYon", yon);
        model.addAttribute("turler",         animeService.tumTurleriGetir());
        model.addAttribute("studyolar",      animeService.tumStudyolariGetir());
        model.addAttribute("yillar",         animeService.tumYillariGetir());
        model.addAttribute("izlemeDurumlari", IzlemeDurumu.values());
        return "anime-listesi";
    }
    @GetMapping("/ekle")
    public String ekleFormu(Authentication authentication, Model model) {
        if (!model.containsAttribute("anime")) {
            model.addAttribute("anime", new Anime());
        }
        model.addAttribute("ualId", null);
        model.addAttribute("seciliDurum", IzlemeDurumu.PLAN_TO_WATCH);
        model.addAttribute("seciliPuan", null);
        model.addAttribute("seciliIzlenenBolum", null);
        model.addAttribute("izlemeDurumlari", IzlemeDurumu.values());
        model.addAttribute("isAdmin", adminMi(authentication));
        return "anime-form";
    }
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
        model.addAttribute("isAdmin", adminMi(authentication));
        return "anime-form";
    }
    @PostMapping("/kaydet")
    public String kaydet(@Valid @ModelAttribute("anime") Anime anime,
                         BindingResult bindingResult,
                         @RequestParam(value = "ualId", required = false) Long ualId,
                         @RequestParam(value = "izlemeDurumu", required = false) IzlemeDurumu izlemeDurumu,
                         @RequestParam(value = "puan", required = false) Integer puan,
                         @RequestParam(value = "izlenenBolum", required = false) Integer izlenenBolum,
                         @RequestParam(value = "kapakDosyasi", required = false) MultipartFile kapakDosyasi,
                         @RequestParam(value = "gorselUrl", required = false) String gorselUrl,
                         @RequestParam(value = "seciliAnimeId", required = false) Long seciliAnimeId,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        User user = oturumKullanicisi(authentication);
        boolean isAdmin = adminMi(authentication);
        try {
            if (ualId == null) {
                if (seciliAnimeId != null) {
                    Anime mevcutAnime = animeService.animeGetir(seciliAnimeId)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Seçilen anime katalogda bulunamadı."));
                    if (!isAdmin && !mevcutAnime.isKatalogaAcik()) {
                        throw new EntityNotFoundException(
                                "Seçilen anime genel katalogda bulunamadı.");
                    }
                    ualService.listeyeEkle(user, mevcutAnime.getId(), izlemeDurumu, puan, izlenenBolum);
                    redirectAttributes.addFlashAttribute("basariMesaji",
                            "\"" + mevcutAnime.getAnimeAdi() + "\" listenize eklendi.");
                } else if (!isAdmin) {
                    if (bindingResult.hasErrors()) {
                        geriDolduFormu(model, ualId, izlemeDurumu, puan, izlenenBolum, isAdmin);
                        return "anime-form";
                    }
                    anime.setKatalogaAcik(false);
                    Anime kaydedilen = animeService.animeKaydetUrlGorselle(anime, null, gorselUrl);
                    ualService.listeyeEkle(user, kaydedilen.getId(), izlemeDurumu, puan, izlenenBolum);
                    redirectAttributes.addFlashAttribute("basariMesaji",
                            "\"" + kaydedilen.getAnimeAdi() + "\" kişisel kütüphanenize eklendi.");
                } else {
                    if (bindingResult.hasErrors()) {
                        geriDolduFormu(model, ualId, izlemeDurumu, puan, izlenenBolum, isAdmin);
                        return "anime-form";
                    }
                    anime.setKatalogaAcik(true);
                    Anime kaydedilen = animeService.animeKaydetUrlGorselle(anime, kapakDosyasi, gorselUrl);
                    redirectAttributes.addFlashAttribute("basariMesaji",
                            "\"" + kaydedilen.getAnimeAdi() + "\" Genel Kataloğa eklendi.");
                    return "redirect:/admin/animeler";
                }
            } else {
                UserAnimeList mevcut = ualService.kayitGetir(ualId, user)
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Liste kaydı bulunamadı: id=" + ualId));
                if (isAdmin) {
                    Anime hedefAnime = mevcut.getAnime();
                    hedefAnime.setAnimeAdi(anime.getAnimeAdi());
                    hedefAnime.setTur(anime.getTur());
                    hedefAnime.setYayinYili(anime.getYayinYili());
                    hedefAnime.setStudyo(anime.getStudyo());
                    hedefAnime.setToplamBolum(anime.getToplamBolum());
                    animeService.animeKaydetUrlGorselle(hedefAnime, kapakDosyasi, gorselUrl);
                }
                ualService.kaydiGuncelle(ualId, user, izlemeDurumu, puan, izlenenBolum);
                redirectAttributes.addFlashAttribute("basariMesaji",
                        "Liste kaydınız güncellendi.");
            }
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("anime.image.error", ex.getMessage());
            geriDolduFormu(model, ualId, izlemeDurumu, puan, izlenenBolum, isAdmin);
            return "anime-form";
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("hataMesaji", ex.getMessage());
            return "redirect:/anime/liste";
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("hataMesaji",
                    "Görsel işlenirken bir hata oluştu: " + ex.getMessage());
            return "redirect:/anime/liste";
        }
        return "redirect:/anime/liste";
    }
    @PostMapping("/sil/{ualId}")
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

    @GetMapping("/api/ara")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> yerelArama(
            @RequestParam(value = "q", required = false) String sorgu) {
        java.util.List<java.util.Map<String, Object>> sonuclar = new java.util.ArrayList<>();
        if (sorgu == null || sorgu.trim().length() < 2) {
            return sonuclar;
        }
        java.util.List<Anime> animeler = animeService.kataloguAra(sorgu.trim(), null, null, null);
        for (Anime a : animeler) {
            java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", a.getId());
            map.put("animeAdi", a.getAnimeAdi());
            map.put("tur", a.getTur());
            map.put("yayinYili", a.getYayinYili());
            map.put("studyo", a.getStudyo());
            map.put("toplamBolum", a.getToplamBolum());
            map.put("gorselVar", a.getKapakGorseli() != null);
            map.put("kaynak", "yerel");
            sonuclar.add(map);
        }
        return sonuclar;
    }
    private User oturumKullanicisi(Authentication authentication) {
        String kullaniciAdi = authentication.getName();
        return userService.kullaniciAdiylaGetir(kullaniciAdi)
                .orElseThrow(() -> new IllegalStateException(
                        "Oturumdaki kullanıcı veritabanında yok: " + kullaniciAdi));
    }
    private String bosluksuzVeyaNull(String s) {
        return (s != null && !s.trim().isEmpty()) ? s.trim() : null;
    }
    private Integer yilParseEt(String yilStr) {
        if (yilStr == null || yilStr.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(yilStr.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
    private IzlemeDurumu durumParseEt(String durumStr) {
        if (durumStr == null || durumStr.trim().isEmpty()) return null;
        try {
            return IzlemeDurumu.valueOf(durumStr.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
    private void geriDolduFormu(Model model, Long ualId,
                                IzlemeDurumu izlemeDurumu, Integer puan, Integer izlenenBolum,
                                boolean isAdmin) {
        model.addAttribute("ualId", ualId);
        model.addAttribute("seciliDurum", izlemeDurumu);
        model.addAttribute("seciliPuan", puan);
        model.addAttribute("seciliIzlenenBolum", izlenenBolum);
        model.addAttribute("izlemeDurumlari", IzlemeDurumu.values());
        model.addAttribute("isAdmin", isAdmin);
    }

    private boolean adminMi(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }
}
