package com.animelistapp.entity;

/**
 * Bir animenin izlenme durumunu temsil eden sabit değerler (Enum).
 * 
 * Veritabanında String olarak saklanır (@Enumerated(EnumType.STRING)).
 * Örnek: "IZLENIYOR", "TAMAMLANDI", "IZLENECEK"
 */
public enum IzlemeDurumu {

    WATCHING("Watching"),
    COMPLETED("Completed"),
    ON_HOLD("On Hold"),
    DROPPED("Dropped"),
    PLAN_TO_WATCH("Plan to Watch");

    /** Kullanıcı arayüzünde gösterilecek etiket */
    private final String etiket;

    IzlemeDurumu(String etiket) {
        this.etiket = etiket;
    }

    public String getEtiket() {
        return etiket;
    }
}
