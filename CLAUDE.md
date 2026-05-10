# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 3.2.5 MVC web app (Java 17 + Thymeleaf + MySQL + Spring Security) for managing personal anime watch lists. Server-rendered templates, BCrypt auth, cover images stored as `LONGBLOB` and served inline as Base64.

## Commands

There is **no Maven wrapper** (`mvnw` / `mvnw.cmd`) in this repo despite the README mentioning one. Running from the command line requires system `mvn` on `PATH`; the project also ships an IntelliJ project (`.idea/`) and the README's recommended path is to run [AnimeListApplication.java](src/main/java/com/animelistapp/AnimeListApplication.java) directly from the IDE. Verify `mvn -version` before assuming CLI builds will work.

```bash
# Run the app (dev)
mvn spring-boot:run

# Build / package
mvn clean package
java -jar target/anime-list-app-1.0.0.jar

# Compile only
mvn compile

# Force-refresh dependencies
mvn clean install -U
```

Prerequisites: MySQL on `localhost:3306` with credentials matching [src/main/resources/application.properties](src/main/resources/application.properties) (currently `root` / `12345`). DB `anime_list_db` auto-creates via `createDatabaseIfNotExist=true`. App listens on port 8080.

There are **no automated tests** (`src/test/` is absent). Do not claim a test run when there is nothing to run — verify changes manually by running the app and exercising the affected flow in the browser.

## Architecture

**Layered MVC**: `controller/` → `service/` (interface + `Impl`) → `repository/` (Spring Data JPA) → `entity/`. Configuration in `config/SecurityConfig.java`. Entry point: [src/main/java/com/animelistapp/AnimeListApplication.java](src/main/java/com/animelistapp/AnimeListApplication.java).

**Startup-time schema cleanup**: `AnimeListApplication` registers a `CommandLineRunner` that attempts `ALTER TABLE animeler DROP COLUMN kullanici_id` on every boot (errors silently caught). This is leftover migration cleanup — don't be surprised by the log line and don't remove it without checking the column is truly gone everywhere. Combined with `spring.jpa.hibernate.ddl-auto=update` and `spring.sql.init.mode=always`, every boot evolves the schema and re-runs `data.sql` (relevant when debugging "why does this row keep coming back").

**Data model & ownership** ([entity/](src/main/java/com/animelistapp/entity)):
- `User` ⇆ `UserAnimeList` ⇆ `Anime`. `UserAnimeList` is the join table holding per-user metadata (status, score, episodes watched, added date).
- `IzlemeDurumu` enum: `WATCHING | COMPLETED | ON_HOLD | DROPPED | PLAN_TO_WATCH`.
- Unique constraint on `(user_id, anime_id)` prevents duplicate entries per user.
- `@PrePersist` on `UserAnimeList` auto-sets `eklenmeTarihi` — don't pass it from controllers.

**Authorization-by-data-ownership pattern** — critical: every read or mutation of `UserAnimeList` is scoped by the authenticated `User` at the repository layer (e.g., `findByKullaniciWithAnime`, `findByIdAndKullanici`, `deleteByIdAndKullanici`). When adding new endpoints or operations, replicate this — never look up by primary key alone, since that would expose another user's records. See [UserAnimeListRepository.java](src/main/java/com/animelistapp/repository/UserAnimeListRepository.java) and [UserAnimeListServiceImpl.java](src/main/java/com/animelistapp/service/UserAnimeListServiceImpl.java). Repository uses JPQL with `JOIN FETCH` to avoid N+1; the `ara` query handles dynamic search/filter/sort.

All mutating service methods are `@Transactional` — preserve this when adding multi-step operations.

**Cover image pipeline** (touch both ends if changing): form submits a URL → [AnimeServiceImpl.java](src/main/java/com/animelistapp/service/AnimeServiceImpl.java) opens an `HttpURLConnection`, validates MIME (`image/jpeg|png|gif|webp`) and a 5MB cap, stores as `byte[]` in `Anime.kapakGorseli` (`@Lob LONGBLOB`). On render, `Anime.getKapakGorseliBase64()` (`@Transient`) Base64-encodes for inline `<img src="data:image/...;base64,...">` in Thymeleaf. There is no static-file storage or CDN. Multipart limits are set to 5MB in `application.properties`.

**Security** ([SecurityConfig.java](src/main/java/com/animelistapp/config/SecurityConfig.java)): permits `/giris`, `/kayit`, `/css/**`, `/js/**`; everything else requires authentication. Form login posts to `/giris-yap` with parameters `kullaniciAdi` and `sifre`; success redirects to `/anime/liste`, failure to `/giris?hata=true`. Logout at `/cikis` invalidates session and clears `JSESSIONID`. Passwords hashed with `BCryptPasswordEncoder`. `CustomUserDetailsService` loads users from `UserRepository`.

**Templates**: 4 Thymeleaf pages in `src/main/resources/templates/` — `giris.html`, `kayit.html`, `anime-listesi.html`, `anime-form.html`. Bootstrap 5.3.3 + Bootstrap Icons via CDN; one local stylesheet at `src/main/resources/static/css/style.css`. Uses `thymeleaf-extras-springsecurity6` (`xmlns:sec`) for role-based rendering. Thymeleaf cache is disabled in dev.

**Note on Jikan API**: The README cites Jikan API integration for anime data, but no HTTP client to Jikan exists in the codebase today. Anime records are user-entered; only cover images are fetched from arbitrary URLs.

## Conventions

- **Turkish identifiers everywhere** — both code (`kullanici`, `sifre`, `izlemeDurumu`, `kapakGorseli`, `eklenmeTarihi`) and URLs (`/giris`, `/kayit`, `/cikis`, `/anime/liste`, `/anime/duzenle/{id}`). Match this style in new code; don't anglicize selectively.
- **Form login parameter names are Turkish** (`kullaniciAdi`, `sifre`) — bound in `SecurityConfig` and must match the `name=` attributes in templates.
- **DB credentials are hardcoded** in `application.properties` (`root` / `12345`). This appears to be a local-dev convention; don't "fix" it in PRs without asking.
