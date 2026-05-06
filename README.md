# 🎌 Anime Listem

Kullanıcıların izledikleri animeleri profesyonel bir şekilde takip edebildikleri, **Jikan API (MyAnimeList)** entegrasyonuna sahip, Spring Boot tabanlı modern bir web uygulamasıdır.

---

## 🛠 Kullanılan Teknolojiler & Sürümler

### Backend
- **Dil:** Java 17
- **Framework:** Spring Boot 3.2.x
- **Güvenlik:** Spring Security
- **Veri Erişim:** Spring Data JPA (Hibernate)
- **Paket Yöneticisi:** Maven

### Veritabanı
- **RDBMS:** MySQL 8+

### Frontend
- **Şablon Motoru:** Thymeleaf
- **UI Kütüphanesi:** Bootstrap 5
- **Dinamik Mantık:** Vanilla JavaScript (Fetch API ile Asenkron İşlemler)
- **Stil & Yapı:** HTML5 / Custom CSS3 (Premium Glassmorphism Design)

### Dış API
- **Veri Kaynağı:** [Jikan API v4](https://docs.api.jikan.moe/) (Resmi olmayan MyAnimeList API'si)

---

## ✨ Proje Mimarisi & Öne Çıkan Özellikler

Uygulama, standart MVC (Model-View-Controller) mimarisine uygun olarak katmanlı bir yapıda geliştirilmiştir (`Controller`, `Service`, `Repository`, `Entity`).

### Mimari Detaylar:
- 🔍 **Akıllı Arama (As-You-Type):** Modal üzerinde anime adını yazarken JavaScript Fetch API ile Jikan API arka planda sorgulanır (Debounce koruması ile). Gelen sonuçlardan seçim yapıldığında form otomatik dolar. Veri tutarlılığı için *Tür, Çıkış Yılı ve Stüdyo* alanları kullanıcı müdahalesine kapalıdır (Readonly).
- 🖼 **Otomatik Görsel İndirme & BLOB Depolama:** Manuel dosya yükleme zorunluluğu kaldırılmıştır. Seçilen animenin resmi API'den gelen URL kullanılarak arka planda (Backend `Service` katmanı) indirilir ve veritabanına doğrudan `byte[]` (BLOB) olarak kaydedilir.
- ⚡ **Dinamik UX Mantığı:** "İzleme Durumu" `(Completed)` seçildiğinde, 'İzlenen Bölüm' sayısı otomatik olarak 'Toplam Bölüm'e eşitlenir. Puanlama arka planda `Integer` (1-10) olarak tutulurken, önyüzde Türkçeleştirilmiş (Örn: *10 - Şaheser*) bir enum mantığıyla çalışır.
- 🛡 **Null-Safe Repository & Exception Handling:** Backend'de dinamik arama yapılırken (isim, tür, yıl bazlı), parametrelerin null veya boş gelmesi ihtimaline karşı alınan güvenlik önlemleri sayesinde uygulama çökmez.
- 🎨 **Modern Arayüz:** Vanilla CSS ile Glassmorphism efektleri, asenkron `Toast` bildirimleri ve mobil uyumlu (responsive) bir grid tasarımı kullanılmıştır.

---

## 🚀 Kurulum ve Çalıştırma Adımları

Projeyi devralıp kendi bilgisayarınızda (Localhost) çalıştırmak için aşağıdaki adımları izleyin:

### 1. Veritabanı Hazırlığı
MySQL sunucunuzu başlatın ve proje için boş bir veritabanı oluşturun:
```sql
CREATE DATABASE anime_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Konfigürasyon Ayarları
`src/main/resources/application.properties` dosyasını açarak veritabanı bilgilerinizi girin:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/anime_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=sifreniz
```

### 3. Şema ve Tablo Oluşumu (Kritik)
Proje ilk kez çalıştırıldığında tabloların (ve test verilerinin) otomatik oluşması için `application.properties` içindeki `ddl-auto` ayarı **`create`** olmalıdır:
```properties
spring.jpa.hibernate.ddl-auto=create
```
> **ÖNEMLİ:** Proje bir kez çalışıp tablolar oluştuktan sonra, her başlatmada verilerin silinmemesi için bu ayarı mutlaka **`update`** olarak değiştirin!

### 4. Uygulamayı Başlatma
IntelliJ IDEA, Eclipse veya VS Code üzerinden `AnimeListApplication.java` sınıfını `Run` komutuyla başlatın. (Alternatif olarak terminalde `mvn spring-boot:run` komutunu kullanabilirsiniz). Uygulama `http://localhost:8080` adresinde ayağa kalkacaktır.

---

## 👤 Test Kullanıcısı (Seed Data)

Uygulama ilk kez `create` modunda ayağa kalktığında `VeriBaslatici` sınıfı devreye girerek veritabanına MAL (MyAnimeList) standartlarında 5 örnek anime ve bir test hesabı tanımlar. Sisteme aşağıdaki bilgilerle giriş yapabilirsiniz:

- **Kullanıcı Adı:** `testuser`
- **Şifre:** `password`

İyi kodlamalar! 💻☕
