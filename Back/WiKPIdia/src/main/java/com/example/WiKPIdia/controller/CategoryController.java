package com.example.WiKPIdia.controller;

import com.example.WiKPIdia.entity.Article;
import com.example.WiKPIdia.entity.Category;
import com.example.WiKPIdia.repository.ArticleRepository;
import com.example.WiKPIdia.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    // ВИПРАВЛЕНО: Прибрано поле description
    public record CategoryCreateRequest(String name, List<Long> articleIds) {}

    // 1. Створення категорії
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.name());

        if (request.articleIds() != null && !request.articleIds().isEmpty()) {
            List<Article> articles = articleRepository.findAllById(request.articleIds());
            if (category.getArticles() != null) {
                category.getArticles().addAll(articles);
            } else {
                category.setArticles(new java.util.HashSet<>(articles));
            }
        }

        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(savedCategory);
    }

    // 2. Отримання категорій (з пошуком тільки за назвою)
    @GetMapping
    public ResponseEntity<?> getAllCategories(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "") String sort) {

        List<Category> categories = categoryRepository.findAll();

        if (!query.isEmpty()) {
            String lowerQuery = query.toLowerCase();
            // ВИПРАВЛЕНО: Шукаємо тільки за назвою
            categories = categories.stream()
                    .filter(c -> c.getName().toLowerCase().contains(lowerQuery))
                    .collect(Collectors.toList());
        }

        if ("asc".equalsIgnoreCase(sort)) {
            categories.sort(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER));
        } else if ("desc".equalsIgnoreCase(sort)) {
            categories.sort(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER).reversed());
        }

        return ResponseEntity.ok(categories);
    }

    // 3. Додавання статті в категорію
    @PostMapping("/{categoryId}/articles/{articleId}")
    public ResponseEntity<?> addArticleToCategory(
            @PathVariable Long categoryId,
            @PathVariable Long articleId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Категорію не знайдено"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Статтю не знайдено"));

        category.getArticles().add(article);
        categoryRepository.save(category);

        return ResponseEntity.ok(Map.of(
                "message", "Статтю '" + article.getTitle() + "' успішно додано до категорії '" + category.getName() + "'"
        ));
    }
    // 4. Метод для отримання ОДНІЄЇ категорії за її ID (з усіма її статтями)
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категорію з ID " + id + " не знайдено."));

        return ResponseEntity.ok(category);
    }
    // 5. Метод ВИЛУЧЕННЯ статті з категорії
    @DeleteMapping("/{categoryId}/articles/{articleId}")
    public ResponseEntity<?> removeArticleFromCategory(
            @PathVariable Long categoryId,
            @PathVariable Long articleId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Категорію не знайдено"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Статтю не знайдено"));

        // Перевіряємо, чи є стаття в цій категорії, і якщо так — видаляємо зв'язок
        if (category.getArticles().contains(article)) {
            category.getArticles().remove(article);
            categoryRepository.save(category);
            return ResponseEntity.ok(Map.of("message", "Статтю успішно вилучено з категорії"));
        } else {
            return ResponseEntity.status(400).body(Map.of("message", "Ця стаття не належить до цієї категорії"));
        }
    }
}