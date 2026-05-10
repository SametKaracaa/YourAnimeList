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

/**
 * Kullanıcı-Anime ilişki tablosuna (kullanici_anime_listesi) erişim.
 *
 * Asıl iş yükünün yapıldığı repository: kullanıcının izleme listesini
 * sayfalar, listeden tek satırı bulur, dinamik arama uygular ve
 * istatistik için sayım yapar.
 *
 * Çoğu sorguda JOIN FETCH ual.anime kullanıyoruz; aksi halde Thymeleaf
 * şablonu liste üzerinde dönerken her satır için ayrı bir SELECT atılır
 * (N+1 problemi).
 */
@Repository
public interface UserAnimeListRepository extends JpaRepository<UserAnimeList, Long> {

    // ============================================================
    // GÜVENLİ ERİŞİM (her zaman kullanıcı parametresiyle)
    // ============================================================

    /**
     * Kullanıcının tüm izleme listesi (anime detayları ile birlikte).
     * Listeyi en son eklenen üstte gelecek şekilde sıralar.
     */
    @Query("""
           SELECT ual FROM UserAnimeList ual
           JOIN FETCH ual.anime
           WHERE ual.kullanici = :kullanici
           """)
    List<UserAnimeList> findByKullaniciWithAnime(@Param("kullanici") User kullanici, org.springframework.data.domain.Sort sort);

    /**
     * Bir liste satırını yalnızca SAHİBİ ise getirir.
     * Başka kullanıcının kaydını /edit, /delete URL'leri ile değiştirmesini engeller.
     */
    @Query("""
           SELECT ual FROM UserAnimeList ual
           JOIN FETCH ual.anime
           WHERE ual.id = :id AND ual.kullanici = :kullanici
           """)
    Optional<UserAnimeList> findByIdAndKullanici(@Param("id") Long id,
                                                 @Param("kullanici") User kullanici);

    /** Aynı kullanıcı aynı animeyi tekrar eklemesin diye kullanılan kontrol. */
    Optional<UserAnimeList> findByKullaniciAndAnime(User kullanici, Anime anime);
    boolean existsByKullaniciAndAnime(User kullanici, Anime anime);


    // ============================================================
    // DİNAMİK ARAMA (kullanıcının KENDİ listesinde)
    // ============================================================
    //
    // Filtre kriterleri (hepsi opsiyonel):
    //   - isim    : anime adında geçen metin (LIKE %x%, case-insensitive)
    //   - tur     : tür içerir
    //   - studyo  : stüdyo içerir
    //   - yil     : yayın yılı (tam eşleşme)
    //   - durum   : izleme durumu (WATCHING / COMPLETED / PLAN_TO_WATCH ...)
    //
    // Anime alanlarına ulaşmak için ual.anime üzerinden join'liyoruz.
    // ============================================================

    /**
     * Kullanıcının izleme listesinde dinamik arama.
     * Boş kalan parametre o kriteri yok sayar.
     */
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


    // ============================================================
    // İSTATİSTİK / DASHBOARD İÇİN
    // ============================================================

    /** "Toplam X anime izliyorsun" gibi istatistikler için. */
    long countByKullanici(User kullanici);

    /** Belirli bir durumdaki kayıt sayısı. (Dashboard kartları için) */
    long countByKullaniciAndIzlemeDurumu(User kullanici, IzlemeDurumu izlemeDurumu);

    /** Kullanıcının verdiği puanların ortalaması (puan girilmemiş satırlar dahil değil). */
    @Query("""
           SELECT AVG(ual.puan) FROM UserAnimeList ual
           WHERE ual.kullanici = :kullanici AND ual.puan IS NOT NULL
           """)
    Double ortalamaPuan(@Param("kullanici") User kullanici);


    // ============================================================
    // SİLME (yalnızca sahibi tarafından)
    // ============================================================

    /**
     * Bir kayıt yalnızca sahibi tarafından silinebilir.
     * @return silinen satır sayısı (0 -> kayıt yok ya da kullanıcıya ait değil)
     *
     * Çağıran @Transactional içinde olmalıdır (genelde service katmanında).
     */
    long deleteByIdAndKullanici(Long id, User kullanici);
}
