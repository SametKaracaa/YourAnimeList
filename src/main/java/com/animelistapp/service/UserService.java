package com.animelistapp.service;
import com.animelistapp.entity.User;
import java.util.List;
import java.util.Optional;
public interface UserService {
    User kullaniciKaydet(User user);
    Optional<User> kullaniciAdiylaGetir(String kullaniciAdi);
    Optional<User> emailIleGetir(String email);
    boolean kullaniciAdiMevcutMu(String kullaniciAdi);
    boolean emailMevcutMu(String email);
    List<User> tumKullanicilariGetir();
    void kullaniciSil(Long id);
    void rolGuncelle(Long id, String yeniRol);
}
