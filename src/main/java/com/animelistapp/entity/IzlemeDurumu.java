package com.animelistapp.entity;

/**
 * Bir animenin izlenme durumunu temsil eden sabit değerler (Enum).
 * 
 * Veritabanında String olarak saklanır (@Enumerated(EnumType.STRING)).
 * Örnek: "IZLENIYOR", "TAMAMLANDI", "IZLENECEK"
 */
public enum IzlemeDurumu {

    WATCHING("İzleniyor"),
    COMPLETED("Tamamlandı"),
    ON_HOLD("Beklemede"),
    DROPPED("Bırakıldı"),
    PLAN_TO_WATCH("İzlenecek");

    /** Kullanıcı arayüzünde gösterilecek etiket */
    private final String etiket;

    IzlemeDurumu(String etiket) {
        this.etiket = etiket;
    }

    public String getEtiket() {
        return etiket;
    }
}
