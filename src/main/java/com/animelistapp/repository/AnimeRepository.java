package com.animelistapp.repository;
import com.animelistapp.entity.Anime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long> {
    Optional<Anime> findByAnimeAdiIgnoreCase(String animeAdi);
    boolean existsByAnimeAdiIgnoreCase(String animeAdi);
    List<Anime> findAllByKatalogaAcikTrueOrderByAnimeAdiAsc();
    @Query("""
           SELECT a FROM Anime a
           WHERE a.katalogaAcik = true
             AND (:isim   IS NULL OR :isim   = '' OR LOWER(a.animeAdi) LIKE LOWER(CONCAT('%', :isim,   '%')))
             AND (:tur    IS NULL OR :tur    = '' OR LOWER(a.tur)      LIKE LOWER(CONCAT('%', :tur,    '%')))
             AND (:studyo IS NULL OR :studyo = '' OR LOWER(a.studyo)   LIKE LOWER(CONCAT('%', :studyo, '%')))
             AND (:yil    IS NULL OR a.yayinYili = :yil)
           ORDER BY a.animeAdi ASC
           """)
    List<Anime> ara(@Param("isim")   String  isim,
                    @Param("tur")    String  tur,
                    @Param("studyo") String  studyo,
                    @Param("yil")    Integer yil);
    @Query("SELECT DISTINCT a.tur FROM Anime a WHERE a.katalogaAcik = true AND a.tur IS NOT NULL AND a.tur <> '' ORDER BY a.tur")
    List<String> findDistinctTurler();
    @Query("SELECT DISTINCT a.studyo FROM Anime a WHERE a.katalogaAcik = true AND a.studyo IS NOT NULL AND a.studyo <> '' ORDER BY a.studyo")
    List<String> findDistinctStudyolar();
    @Query("SELECT DISTINCT a.yayinYili FROM Anime a WHERE a.katalogaAcik = true AND a.yayinYili IS NOT NULL ORDER BY a.yayinYili DESC")
    List<Integer> findDistinctYillar();
}
