package com.example.WiKPIdia.repository;

import com.example.WiKPIdia.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;     // Додаємо імпорт для списків
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    // Твій попередній метод (щоб працювало збереження і категорії)
    Optional<Article> findBySlug(String slug);

    // ДОДАЙ ЦЕЙ РЯДОК (щоб працював SearchController):
    List<Article> findByIsPublishedTrue();

}