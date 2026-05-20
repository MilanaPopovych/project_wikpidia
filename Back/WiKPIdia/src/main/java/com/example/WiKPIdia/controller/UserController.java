package com.example.WiKPIdia.controller;

import com.example.WiKPIdia.entity.Article;
import com.example.WiKPIdia.entity.SavedArticle;
import com.example.WiKPIdia.entity.User;
import com.example.WiKPIdia.repository.ArticleRepository;
import com.example.WiKPIdia.repository.SavedArticleRepository;
import com.example.WiKPIdia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class UserController {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final SavedArticleRepository savedArticleRepository;

    private String getActiveUsernameSafely() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String activeUsername = null;
        if (principal instanceof UserDetails) {
            activeUsername = ((UserDetails) principal).getUsername();
        } else if (principal != null && !"anonymousUser".equals(principal.toString())) {
            activeUsername = principal.toString();
        }
        if (activeUsername == null && AuthController.currentSessionUser != null && !AuthController.currentSessionUser.isEmpty()) {
            activeUsername = AuthController.currentSessionUser;
        }
        return activeUsername;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        String activeUsername = getActiveUsernameSafely();
        if (activeUsername == null || "anonymousUser".equals(activeUsername)) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизовано"));
        }

        User currentUser = userRepository.findByUsername(activeUsername).orElse(null);
        if (currentUser == null) return ResponseEntity.status(404).body(Map.of("error", "Користувача не знайдено"));

        Map<String, Object> userInfo = Map.of(
                "username", currentUser.getUsername(),
                "fullName", currentUser.getFullName() != null ? currentUser.getFullName() : "",
                "email", currentUser.getEmail() != null ? currentUser.getEmail() : "",
                "role", currentUser.getRole() != null ? currentUser.getRole() : "Користувач",
                "createdAt", currentUser.getCreatedAt() != null ? currentUser.getCreatedAt() : "Сьогодні"
        );

        List<Article> userArticles = articleRepository.findByAuthor(activeUsername);
        List<Map<String, String>> recentPublications = new ArrayList<>();
        for (Article article : userArticles) {
            recentPublications.add(Map.of(
                    "id", String.valueOf(article.getId()),
                    "title", article.getTitle(),
                    "slug", article.getSlug(),
                    "type", "Стаття",
                    "date", "20.05.2026"
            ));
        }
        // підтягуємо збережені статті з нової таблиці бд
        List<SavedArticle> savedDbArticles = savedArticleRepository.findByUsernameOrderByIdDesc(activeUsername);
        List<Map<String, String>> savedArticles = new ArrayList<>();
        for (SavedArticle sa : savedDbArticles) {
            savedArticles.add(Map.of(
                    "id", String.valueOf(sa.getId()),
                    "title", sa.getArticleTitle(),
                    "slug", sa.getArticleSlug(),
                    "savedAt", sa.getSavedAt()
            ));
        }

        return ResponseEntity.ok(Map.of("userInfo", userInfo, "recentPublications", recentPublications, "savedArticles", savedArticles));
    }

    // !!!!! отримання історії обговорень користувача !!!!!
    @GetMapping("/discussions")
    public ResponseEntity<?> getUserDiscussions() {
        String activeUsername = getActiveUsernameSafely();

        if (activeUsername == null || "anonymousUser".equals(activeUsername)) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизовано"));
        }
        // --- ВАРІАНТ 1: Реальне підключення до бази даних ---
        // (Якщо у вас є сутність коментарів, підключіть репозиторій і розкоментуйте цей блок)
        /*
        List<Discussion> userDiscussions = discussionRepository.findByAuthorOrderByIdDesc(activeUsername);
        List<Map<String, Object>> response = new ArrayList<>();

        for (Discussion disc : userDiscussions) {
            response.add(Map.of(
                    "id", disc.getId(),
                    "articleTitle", disc.getArticle() != null ? disc.getArticle().getTitle() : "Без назви",
                    "articleSlug", disc.getArticle() != null ? disc.getArticle().getSlug() : "",
                    "topic", disc.getTopic() != null ? disc.getTopic() : "Загальне обговорення",
                    "comment", disc.getContent(), // або disc.getComment()
                    "createdAt", disc.getCreatedAt() != null ? disc.getCreatedAt() : "Сьогодні"
            ));
        }
        return ResponseEntity.ok(response);
        */

        // --- ВАРІАНТ 2: Тимчасова заглушка для перевірки роботи фронтенду ---
        List<Map<String, Object>> mockDiscussions = new ArrayList<>();

        mockDiscussions.add(Map.of(
                "id", 1,
                "articleTitle", "Основи алгоритмів",
                "articleSlug", "osnovy-algorytmiv",
                "topic", "Питання щодо складності",
                "comment", "Чи не могли б ви додати інформацію про просторову складність O(n)?",
                "createdAt", "20.05.2026"
        ));

        mockDiscussions.add(Map.of(
                "id", 2,
                "articleTitle", "Архітектура фон Неймана",
                "articleSlug", "arhitektura-fon-neymana",
                "topic", "Уточнення факту",
                "comment", "Здається, у другому абзаці є неточність щодо шини даних. Можете пояснити детальніше?",
                "createdAt", "19.05.2026"
        ));

        return ResponseEntity.ok(mockDiscussions);
    }

    // =========================================================================
    // НОВІ ЕНДПОІНТИ: КЕРУВАННЯ ЗБЕРЕЖЕНИМИ СТАТТЯМИ
    // =========================================================================

    @GetMapping("/saved/{slug}/check")
    public ResponseEntity<?> checkSaved(@PathVariable String slug) {
        String activeUsername = getActiveUsernameSafely();
        boolean isSaved = savedArticleRepository.existsByUsernameAndArticleSlug(activeUsername, slug);
        return ResponseEntity.ok(Map.of("isSaved", isSaved));
    }

    @PostMapping("/saved")
    public ResponseEntity<?> saveArticle(@RequestBody Map<String, String> request) {
        String activeUsername = getActiveUsernameSafely();
        if (activeUsername == null || "anonymousUser".equals(activeUsername)) return ResponseEntity.status(401).build();

        String slug = request.get("slug");
        String title = request.get("title");

        if (!savedArticleRepository.existsByUsernameAndArticleSlug(activeUsername, slug)) {
            SavedArticle sa = new SavedArticle();
            sa.setUsername(activeUsername);
            sa.setArticleSlug(slug);
            sa.setArticleTitle(title);
            sa.setSavedAt(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            savedArticleRepository.save(sa);
        }
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/saved/{slug}")
    public ResponseEntity<?> unsaveArticle(@PathVariable String slug) {
        String activeUsername = getActiveUsernameSafely();
        if (activeUsername != null && !"anonymousUser".equals(activeUsername)) {
            savedArticleRepository.deleteByUsernameAndArticleSlug(activeUsername, slug);
        }
        return ResponseEntity.ok().build();
    }
}
