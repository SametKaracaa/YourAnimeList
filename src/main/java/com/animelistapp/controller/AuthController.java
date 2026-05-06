package com.animelistapp.controller;

import com.animelistapp.entity.User;
import com.animelistapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Kayıt + giriş sayfalarını yöneten controller.
 *
 * Önemli notlar:
 *   - Spring Security login işlemini KENDİSİ yapar (POST /giris-yap),
 *     biz sadece formu gösterirken Controller'a uğruyoruz.
 *   - Kayıt akışında @Valid ile entity validation çalışır;
 *     hatalar BindingResult ile forma geri yansıtılır.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** Anasayfa → kullanıcının izleme listesine yönlendir. */
    @GetMapping("/")
    public String anaSayfa() {
        return "redirect:/anime/liste";
    }

    /** Giriş formu — Spring Security loginPage("/giris") ile bağlandı. */
    @GetMapping("/giris")
    public String girisFormu() {
        return "giris";
    }

    /** Boş bir User objesi ile kayıt formunu açar. */
    @GetMapping("/kayit")
    public String kayitFormu(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "kayit";
    }

    /**
     * Kayıt POST işlemi.
     *
     * Akış:
     *   1) @Valid → entity validation (NotBlank, Size, Email vb.) çalışır.
     *   2) BindingResult'ta hata varsa formu hatalarla yeniden render et.
     *   3) UserService kayıt yapar; çakışma varsa IllegalArgumentException atar
     *      → onu yakalayıp ilgili field'a binding error olarak ekleriz.
     */
    @PostMapping("/kayit")
    public String kayitIslemi(@Valid @ModelAttribute("user") User user,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        // 1) Validation hatası varsa formu yeniden aç
        if (bindingResult.hasErrors()) {
            return "kayit";
        }

        // 2) Servis seviyesinde unique kontrolü — race condition'a karşı son savunma hattı
        try {
            userService.kullaniciKaydet(user);
        } catch (IllegalArgumentException ex) {
            // Hangi field çakıştıysa ona ata (mesaja bakarak — basit ayrım)
            String mesaj = ex.getMessage();
            if (mesaj != null && mesaj.toLowerCase().contains("kullanıcı adı")) {
                bindingResult.rejectValue("kullaniciAdi", "duplicate", mesaj);
            } else if (mesaj != null && mesaj.toLowerCase().contains("e-posta")) {
                bindingResult.rejectValue("email", "duplicate", mesaj);
            } else {
                bindingResult.reject("registration.error", mesaj);
            }
            return "kayit";
        }

        redirectAttributes.addFlashAttribute("basariMesaji",
                "Kayıt başarılı! Şimdi giriş yapabilirsiniz.");
        return "redirect:/giris";
    }
}
