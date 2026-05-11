package com.animelistapp.controller;

import com.animelistapp.entity.User;
import com.animelistapp.repository.UserAnimeListRepository;
import com.animelistapp.service.AnimeService;
import com.animelistapp.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final AnimeService animeService;
    private final UserAnimeListRepository ualRepository;

    public AdminController(UserService userService,
                           AnimeService animeService,
                           UserAnimeListRepository ualRepository) {
        this.userService = userService;
        this.animeService = animeService;
        this.ualRepository = ualRepository;
    }

    @GetMapping("/panel")
    public String panel(Authentication authentication, Model model) {
        model.addAttribute("adminAdi", authentication.getName());
        model.addAttribute("toplamKullanici", userService.tumKullanicilariGetir().size());
        model.addAttribute("toplamAnime", animeService.tumKatalog().size());
        model.addAttribute("toplamKayit", ualRepository.count());
        return "admin-panel";
    }

    @GetMapping("/kullanicilar")
    public String kullanicilar(Authentication authentication, Model model) {
        model.addAttribute("adminAdi", authentication.getName());
        model.addAttribute("kullanicilar", userService.tumKullanicilariGetir());
        return "admin-kullanicilar";
    }

    @PostMapping("/kullanici/sil/{id}")
    public String kullaniciSil(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        User hedef = userService.tumKullanicilariGetir().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst().orElse(null);
        if (hedef != null && hedef.getKullaniciAdi().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("hataMesaji",
                    "Kendi hesabınızı silemezsiniz!");
            return "redirect:/admin/kullanicilar";
        }
        if (hedef != null && "admin".equalsIgnoreCase(hedef.getKullaniciAdi())) {
            redirectAttributes.addFlashAttribute("hataMesaji",
                    "Sistem yöneticisi hesabı silinemez!");
            return "redirect:/admin/kullanicilar";
        }
        try {
            userService.kullaniciSil(id);
            redirectAttributes.addFlashAttribute("basariMesaji", "Kullanıcı silindi.");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("hataMesaji", ex.getMessage());
        }
        return "redirect:/admin/kullanicilar";
    }

    @PostMapping("/kullanici/rol/{id}")
    public String rolDegistir(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        User hedef = userService.tumKullanicilariGetir().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst().orElse(null);
        if (hedef == null) {
            redirectAttributes.addFlashAttribute("hataMesaji", "Kullanıcı bulunamadı.");
            return "redirect:/admin/kullanicilar";
        }
        if (hedef.getKullaniciAdi().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("hataMesaji",
                    "Kendi rolünüzü değiştiremezsiniz!");
            return "redirect:/admin/kullanicilar";
        }
        if ("admin".equalsIgnoreCase(hedef.getKullaniciAdi())) {
            redirectAttributes.addFlashAttribute("hataMesaji",
                    "Sistem yöneticisinin rolü değiştirilemez!");
            return "redirect:/admin/kullanicilar";
        }
        if ("ROLE_ADMIN".equals(hedef.getRol())) {
            userService.rolGuncelle(id, "ROLE_USER");
            redirectAttributes.addFlashAttribute("basariMesaji",
                    hedef.getKullaniciAdi() + " kullanıcısının rolü 'ROLE_USER' olarak güncellendi.");
        } else {
            redirectAttributes.addFlashAttribute("hataMesaji",
                    "Başka bir kullanıcıya admin rolü verilemez. Sistemde yalnızca bir admin bulunabilir.");
        }
        return "redirect:/admin/kullanicilar";
    }

    @GetMapping("/animeler")
    public String animeler(Authentication authentication, Model model) {
        model.addAttribute("adminAdi", authentication.getName());
        model.addAttribute("animeler", animeService.tumKatalog());
        return "admin-animeler";
    }

    @PostMapping("/anime/sil/{id}")
    public String animeSil(@PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        try {
            animeService.animeSil(id);
            redirectAttributes.addFlashAttribute("basariMesaji", "Anime katalogdan silindi.");
        } catch (EntityNotFoundException ex) {
            redirectAttributes.addFlashAttribute("hataMesaji", ex.getMessage());
        }
        return "redirect:/admin/animeler";
    }
}
