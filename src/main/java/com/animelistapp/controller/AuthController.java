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
@Controller
public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/")
    public String anaSayfa() {
        return "redirect:/anime/liste";
    }
    @GetMapping("/giris")
    public String girisFormu() {
        return "giris";
    }
    @GetMapping("/kayit")
    public String kayitFormu(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "kayit";
    }
    @PostMapping("/kayit")
    public String kayitIslemi(@Valid @ModelAttribute("user") User user,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "kayit";
        }
        try {
            userService.kullaniciKaydet(user);
        } catch (IllegalArgumentException ex) {
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
