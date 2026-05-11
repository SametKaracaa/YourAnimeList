# YourAnimeList Backend (Spring Boot)

Bu proje, kullanıcıların anime listelerini yönetmelerini, yeni animeler keşfetmelerini ve izleme durumlarını (İzleniyor, Tamamlandı, vb.) kaydetmelerini sağlayan bir **Spring Boot MVC** uygulamasıdır.

## 📂 Dizin Yapısı ve Sınıflar (`src/main/java/com/animelistapp`)

### 1. `config/` (Yapılandırma)
- **`SecurityConfig.java`**: Spring Security yapılandırmasını içerir. Kullanıcı giriş/çıkış işlemleri, yetkilendirmeler ve public/private rotalar burada belirlenir. Şifrelerin güvenli saklanması için `BCryptPasswordEncoder` kullanılır.

### 2. `controller/` (İstek Karşılayıcılar - MVC)
- **`AnimeController.java`**: Anime listesi, anime ekleme, silme ve düzenleme sayfalarının (`/anime/...`) GET ve POST isteklerini yönetir. Jikan API ve veritabanı arasındaki bağlantıyı koordine eder. Arama, filtreleme ve sıralama gibi UI etkileşimlerini servislere aktarır.
- **`AuthController.java`**: Kullanıcı kayıt (`/kayit`) ve giriş (`/giris`) sayfalarını yönetir.

### 3. `entity/` (Veritabanı Tabloları)
- **`Anime.java`**: Sistemdeki tüm animelerin temel bilgilerini (isim, yıl, stüdyo, tür, kapak görseli BLOB verisi) tutan global tablodur.
- **`User.java`**: Sistemi kullanan kullanıcıların hesap bilgilerini (kullanıcı adı, şifre, rol) tutar.
- **`UserAnimeList.java`**: Kullanıcı ile Anime arasındaki köprü tablodur. Kullanıcının listesine eklediği animenin durumunu, puanını, izlediği bölüm sayısını ve ekleme tarihini (metadata) tutar.
- **`IzlemeDurumu.java`** (Enum): Animenin mevcut izleme durumunu belirtir (İzleniyor, Tamamlandı, Beklemede, Bırakıldı, İzlenecek).

### 4. `repository/` (Veritabanı İşlemleri)
Spring Data JPA arayüzleridir.
- **`AnimeRepository.java`**: Animeleri ismine veya MyAnimeList ID'sine göre bulmak için arama metotları barındırır.
- **`UserRepository.java`**: Kullanıcı adı üzerinden db kaydı bulma (giriş yaparken) işlemleri için kullanılır.
- **`UserAnimeListRepository.java`**: Kullanıcıya ait listeyi filtreleme (isim, tür, çıkış tarihi vb.) ve dinamik sıralama işlemleri için JPQL (`JOIN FETCH`) tabanlı özel ve optimize sorgular içerir.

### 5. `service/` (İş Kuralları - Business Logic)
- **`AnimeService.java` & `AnimeServiceImpl.java`**: Yeni bir anime eklenirken, dışarıdan gelen kapak görseli URL'sini indirip veritabanına (BLOB olarak) kaydetme gibi işlemleri üstlenir.
- **`UserService.java` & `UserServiceImpl.java`**: Yeni kullanıcı kaydı sırasında şifrenin bcrypt ile hash'lenip veritabanına eklenmesini sağlar.
- **`CustomUserDetailsService.java`**: Spring Security'nin login ekranında girilen verileri veritabanı ile eşleştirerek doğrulaması için gereken sınıftır.
- **`UserAnimeListService.java` & `UserAnimeListServiceImpl.java`**: Listeye anime ekleme, çıkarma, güncelleme ve arama algoritmalarını yönetir. Her işlemin sadece listeyi oluşturan kullanıcı tarafından yapılabildiğini (Authorization-Data ownership check) doğrular.

## 🚀 Çalıştırma ve Kullanım

### Gereksinimler
- **MySQL**: Veritabanı bağlantısı için `src/main/resources/application.properties` dosyasındaki kullanıcı adı ve şifreyi kendi sisteminize göre düzeltin (varsayılan olarak `root` / `1234` yapılandırılmıştır).

### Çalıştırma Talimatı
1. Proje kök dizininde terminali açın.
2. `mvn spring-boot:run` komutunu çalıştırın veya IDE'niz üzerinden `AnimeListApplication` sınıfını başlatın.
3. Tarayıcıdan `http://localhost:8080` adresine gidin.

### Test Kullanıcısı
Uygulama başlatıldığında `data.sql` üzerinden örnek veriler yüklenir. Aşağıdaki hesapla giriş yapabilirsiniz:
- **Kullanıcı Adı:** `testuser`
- **Şifre:** `password`

### Önemli Notlar
- **Görsel Ekleme Yöntemleri:** Anime eklerken iki yöntem kullanabilirsiniz:
  1. **Jikan Araması:** Üst arama kutusuna anime adını yazarak (en az 3 karakter) Jikan'dan otomatik veri ve görsel URL'si çekebilirsiniz.
  2. **Yerel Dosya Yükleme:** Form içindeki dosya seçici ile bilgisayarınızdan manuel olarak kapak görseli yükleyebilirsiniz.
- **BLOB Depolama:** Görseller dosya sistemine yazılmaz; veritabanında `LONGBLOB` formatında güvenli bir şekilde saklanır.
