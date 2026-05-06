package com.animelistapp.service;

import com.animelistapp.entity.User;

import java.util.Optional;

/**
 * Kullanıcı işlemlerini tanımlayan servis arayüzü.
 *
 * Controller'lar bu arayüze bağımlıdır, somut sınıfa değil
 * (Dependency Inversion / Loose Coupling).
 */
public interface UserService {

    /**
     * Yeni kullanıcı kaydı.
     * Şifre, BCrypt ile hashlenip DB'ye o haliyle yazılır.
     *
     * @throws IllegalArgumentException kullanıcı adı veya e-posta zaten varsa
     */
    User kullaniciKaydet(User user);

    Optional<User> kullaniciAdiylaGetir(String kullaniciAdi);

    Optional<User> emailIleGetir(String email);

    boolean kullaniciAdiMevcutMu(String kullaniciAdi);

    boolean emailMevcutMu(String email);
}
