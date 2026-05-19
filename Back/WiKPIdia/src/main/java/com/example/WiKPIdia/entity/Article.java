package com.example.WiKPIdia.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // ДОДАНО ІМПОРТ
import java.util.ArrayList; // ДОДАНО ІМПОРТ
import java.util.List; // ДОДАНО ІМПОРТ

@Entity
@Table(name = "articles")
@Data
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    private String mainImage;
    private String author;
    private String version;
    private String comment;

    // ДОДАНО: Зв'язок із категоріями, який буде відправлятися на фронтенд
    @ManyToMany(mappedBy = "articles", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("articles")
    private List<Category> categories = new ArrayList<>();
}