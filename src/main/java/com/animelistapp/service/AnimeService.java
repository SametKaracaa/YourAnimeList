package com.animelistapp.service;
import com.animelistapp.entity.Anime;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
public interface AnimeService {
    Anime animeKaydet(Anime anime, MultipartFile kapakDosyasi) throws IOException;
    Anime animeKaydetUrlGorselle(Anime anime, MultipartFile kapakDosyasi, String gorselUrl) throws IOException;
    Optional<Anime> animeGetir(Long id);
    Anime animeGetirOrThrow(Long id);
    List<Anime> tumKatalog();
    void animeSil(Long id);
    List<Anime> kataloguAra(String isim, String tur, String studyo, Integer yil);
    List<String> tumTurleriGetir();
    List<String> tumStudyolariGetir();
    List<Integer> tumYillariGetir();
}
