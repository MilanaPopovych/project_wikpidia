package com.example.WiKPIdia.repository;

import com.example.WiKPIdia.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findBySlug(String slug); // пошук однієї статті за слагом
    List<Article> findByAuthor(String author); // для пошуку всіх статей конкретного автора
    List<Article> findByIsPublishedTrue(); // виведення тільки опублікованих статей на головну
    List<Article> findByIsPublishedFalse();
}