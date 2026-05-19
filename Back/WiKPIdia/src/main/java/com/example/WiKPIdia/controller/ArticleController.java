package com.example.WiKPIdia.controller;

import com.example.WiKPIdia.entity.Article;
import com.example.WiKPIdia.entity.User;
import com.example.WiKPIdia.repository.ArticleRepository;
import com.example.WiKPIdia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public record ArticleRequest(String title, String content, String comment) {}

    // 1. ЧИТАННЯ ВСІХ: Отримати всі опубліковані статті
    @GetMapping
    public List<Article> getPublishedArticles() {
        return articleRepository.findByIsPublishedTrue();
    }

    // 2. ЧИТАННЯ ОДНІЄЇ: Отримання статті за її SLUG (щоб збігалося з фронтендом!)
    @GetMapping("/{slug}")
    public Article getArticleBySlug(@PathVariable String slug) {
        return articleRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Статтю не знайдено"));
    }

    // 3. СТВОРЕННЯ: Додати нову статтю + автоматичний SLUG
    @PostMapping
    public Article createArticle(@RequestBody ArticleRequest request) {
        Article article = new Article();
        article.setTitle(request.title());
        article.setContent(request.content());

        // ДОДАНО: Автоматично генеруємо і записуємо слаг перед збереженням
        article.setSlug(generateSlug(request.title()));

        System.out.println("Коментар до статті: " + request.comment());
        article.setIsPublished(true);

        return articleRepository.save(article);
    }

    // 4. ВИДАЛЕННЯ: Видалити статтю за SLUG (ТІЛЬКИ ДЛЯ АДМІНІСТРАТОРА)
    @DeleteMapping("/{slug}")
    public ResponseEntity<?> deleteArticle(@PathVariable String slug) {

        // 1. Перевіряємо, хто зараз сидить на сайті
        String activeUsername = AuthController.currentSessionUser;
        User currentUser = userRepository.findByUsername(activeUsername).orElse(null);

        // 2. БЛОКУЄМО ДОСТУП, якщо це не Адміністратор (перевіряємо обидва варіанти)
        if (currentUser == null || !("Адміністратор".equals(currentUser.getRole()) || "Адмін".equals(currentUser.getRole()))) {
            return ResponseEntity.status(403).body(Map.of("message", "Доступ заборонено! Тільки Адміністратор може видаляти статті."));
        }

        // 3. Якщо перевірка пройдена, шукаємо статтю
        Article article = articleRepository.findBySlug(slug).orElse(null);
        if (article == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Статтю не знайдено."));
        }

        // 4. Видаляємо статтю
        articleRepository.delete(article);

        return ResponseEntity.ok(Map.of("message", "Статтю успішно видалено адміністратором."));
    }



    // Метод для автоматичної генерації URL (слагу) з назви статті
    private String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return "article-" + System.currentTimeMillis();
        }

        String slug = title.toLowerCase();

        // Транслітерація українських літер
        String[] ukr = {"а", "б", "в", "г", "ґ", "д", "е", "є", "ж", "з", "и", "і", "ї", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ч", "ш", "щ", "ь", "ю", "я"};
        String[] lat = {"a", "b", "v", "h", "g", "d", "e", "ie", "zh", "z", "y", "i", "yi", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "kh", "ts", "ch", "sh", "shch", "", "iu", "ia"};

        for (int i = 0; i < ukr.length; i++) {
            slug = slug.replace(ukr[i], lat[i]);
        }

        // Залишаємо тільки латиницю, цифри та дефіси
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.replaceAll("\\s+", "-"); // Замінюємо пробіли на дефіси
        slug = slug.replaceAll("-+", "-"); // Прибираємо подвійні дефіси

        // Додаємо 4 випадкові цифри в кінці, щоб слаг завжди був унікальним
        return slug + "-" + (System.currentTimeMillis() % 10000);
    }
}