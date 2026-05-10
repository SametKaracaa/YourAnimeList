package com.animelistapp.repository;
import com.animelistapp.entity.Anime;
import com.animelistapp.entity.IzlemeDurumu;
import com.animelistapp.entity.User;
import com.animelistapp.entity.UserAnimeList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface UserAnimeListRepository extends JpaRepository<UserAnimeList, Long> {
    @Query("""
           SELECT ual FROM UserAnimeList ual
           JOIN FETCH ual.anime
           WHERE ual.kullanici = :kullanici
           """)
    List<UserAnimeList> findByKullaniciWithAnime(@Param("kullanici") User kullanici, org.springframework.data.domain.Sort sort);
    @Query("""
           SELECT ual FROM UserAnimeList ual
           JOIN FETCH ual.anime
           WHERE ual.id = :id AND ual.kullanici = :kullanici
           """)
    Optional<UserAnimeList> findByIdAndKullanici(@Param("id") Long id,
                                                 @Param("kullanici") User kullanici);
    Optional<UserAnimeList> findByKullaniciAndAnime(User kullanici, Anime anime);
    boolean existsByKullaniciAndAnime(User kullanici, Anime anime);
    @Query("""
           SELECT ual FROM UserAnimeList ual
           JOIN FETCH ual.anime a
           WHERE ual.kullanici = :kullanici
             AND (:isim   IS NULL OR :isim   = '' OR LOWER(a.animeAdi) LIKE LOWER(CONCAT('%', :isim,   '%')))
             AND (:tur    IS NULL OR :tur    = '' OR LOWER(a.tur)      LIKE LOWER(CONCAT('%', :tur,    '%')))
             AND (:studyo IS NULL OR :studyo = '' OR LOWER(a.studyo)   LIKE LOWER(CONCAT('%', :studyo, '%')))
             AND (:yil    IS NULL OR a.yayinYili = :yil)
             AND (:durum  IS NULL OR ual.izlemeDurumu = :durum)
           """)
    List<UserAnimeList> ara(@Param("kullanici") User         kullanici,
                            @Param("isim")      String       isim,
                            @Param("tur")       String       tur,
                            @Param("studyo")    String       studyo,
                            @Param("yil")       Integer      yil,
                            @Param("durum")     IzlemeDurumu durum,
                            org.springframework.data.domain.Sort sort);
    long countByKullanici(User kullanici);
    long countByKullaniciAndIzlemeDurumu(User kullanici, IzlemeDurumu izlemeDurumu);
    @Query("""
           SELECT AVG(ual.puan) FROM UserAnimeList ual
           WHERE ual.kullanici = :kullanici AND ual.puan IS NOT NULL
           """)
    Double ortalamaPuan(@Param("kullanici") User kullanici);
    long deleteByIdAndKullanici(Long id, User kullanici);
}
