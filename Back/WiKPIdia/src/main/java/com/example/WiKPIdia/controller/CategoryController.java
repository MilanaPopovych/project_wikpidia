package com.example.WiKPIdia.controller;

import com.example.WiKPIdia.entity.Article;
import com.example.WiKPIdia.entity.Category;
import com.example.WiKPIdia.repository.ArticleRepository;
import com.example.WiKPIdia.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    // 1. Метод для створення нової категорії
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(savedCategory);
    }

    // ДОДАНО: Метод для отримання списку ВСІХ категорій (щоб працював випадаючий список на сайті)
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 2. Метод додавання статті в категорію
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
}