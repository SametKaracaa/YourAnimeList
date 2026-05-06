package com.animelistapp.repository;

import com.animelistapp.entity.Anime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Anime kataloğuna (animeler) erişim sağlayan repository.
 *
 * Anime tablosu artık "paylaşılan katalog" — kullanıcı bağımsızdır.
 * Bu repository hem kataloğa yeni anime eklerken duplikasyonu kontrol eder,
 * hem de "tüm sistemdeki animelerde" dinamik arama imkânı sunar.
 */
@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long> {

    // ============================================================
    // BASİT TÜRETİLMİŞ (DERIVED) SORGULAR
    // ============================================================

    /** Aynı isimle (büyük/küçük harf bağımsız) anime var mı? */
    Optional<Anime> findByAnimeAdiIgnoreCase(String animeAdi);

    /** Eklemeden önce isim çakışması kontrolü için. */
    boolean existsByAnimeAdiIgnoreCase(String animeAdi);

    /** Listeleme/dropdown için ada göre alfabetik sıralı tüm animeler. */
    List<Anime> findAllByOrderByAnimeAdiAsc();


    // ============================================================
    // DİNAMİK ARAMA (İSİM + TÜR + STÜDYO + YIL)
    // ============================================================
    //
    // Tüm parametreler OPSİYONELDİR.
    //   Her filtre için kalıp:  (:p IS NULL OR :p = '' OR <koşul>)
    //   - Parametre null veya boş ise o kriterden filtreleme YAPILMAZ.
    //   - Yıl için boş string anlamlı değil; sadece NULL kontrolü yapılır.
    //
    // String karşılaştırmalar LOWER + LIKE %x% ile yapılır,
    // bu sayede "JuJu" araması "Jujutsu Kaisen" sonucu da getirir.
    // ============================================================

    /**
     * Animelerin tamamı üzerinde dinamik arama.
     *
     * @param isim   anime adında geçen metin (içerir, case-insensitive). null/"" => filtreleme yok
     * @param tur    türde geçen metin (içerir). null/"" => filtreleme yok
     * @param studyo stüdyoda geçen metin (içerir). null/"" => filtreleme yok
     * @param yil    yayın yılı (TAM EŞLEŞME). null => filtreleme yok
     * @return Kriterlere uyan animeler — sonuç ada göre sıralı döner
     */
    @Query("""
           SELECT a FROM Anime a
           WHERE (:isim   IS NULL OR :isim   = '' OR LOWER(a.animeAdi) LIKE LOWER(CONCAT('%', :isim,   '%')))
             AND (:tur    IS NULL OR :tur    = '' OR LOWER(a.tur)      LIKE LOWER(CONCAT('%', :tur,    '%')))
             AND (:studyo IS NULL OR :studyo = '' OR LOWER(a.studyo)   LIKE LOWER(CONCAT('%', :studyo, '%')))
             AND (:yil    IS NULL OR a.yayinYili = :yil)
           ORDER BY a.animeAdi ASC
           """)
    List<Anime> ara(@Param("isim")   String  isim,
                    @Param("tur")    String  tur,
                    @Param("studyo") String  studyo,
                    @Param("yil")    Integer yil);


    // ============================================================
    // FİLTRE DROPDOWN'LARI İÇİN YARDIMCI SORGULAR
    // ============================================================

    /** Arama formundaki "Tür" dropdown'ı için benzersiz türleri getirir. */
    @Query("SELECT DISTINCT a.tur FROM Anime a WHERE a.tur IS NOT NULL AND a.tur <> '' ORDER BY a.tur")
    List<String> findDistinctTurler();

    /** "Stüdyo" dropdown'ı için. */
    @Query("SELECT DISTINCT a.studyo FROM Anime a WHERE a.studyo IS NOT NULL AND a.studyo <> '' ORDER BY a.studyo")
    List<String> findDistinctStudyolar();

    /** "Yıl" dropdown'ı için (en yeniden eskiye). */
    @Query("SELECT DISTINCT a.yayinYili FROM Anime a WHERE a.yayinYili IS NOT NULL ORDER BY a.yayinYili DESC")
    List<Integer> findDistinctYillar();
}
