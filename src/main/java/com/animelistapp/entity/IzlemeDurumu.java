package com.animelistapp.entity;
public enum IzlemeDurumu {
    WATCHING("İzleniyor"),
    COMPLETED("Tamamlandı"),
    ON_HOLD("Beklemede"),
    DROPPED("Bırakıldı"),
    PLAN_TO_WATCH("İzlenecek");
    private final String etiket;
    IzlemeDurumu(String etiket) {
        this.etiket = etiket;
    }
    public String getEtiket() {
        return etiket;
    }
}
