package com.example.WiKPIdia.controller;

import com.example.WiKPIdia.entity.Discussion;
import com.example.WiKPIdia.repository.DiscussionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionRepository discussionRepository;

    // Отримання всіх коментарів для статті
    @GetMapping("/{slug}/discussions")
    public List<Discussion> getDiscussionsByArticle(@PathVariable String slug) {
        return discussionRepository.findByArticleSlugOrderByCreatedAtDesc(slug);
    }

    // Створення нового коментаря
    @PostMapping("/{slug}/discussions")
    public ResponseEntity<Discussion> addDiscussion(
            @PathVariable String slug,
            @RequestBody Discussion discussion) {

        // 1. Прив'язуємо коментар до конкретної статті
        discussion.setArticleSlug(slug);

        // 2. Дістаємо логін того, хто зараз сидить на сайті
        String activeUsername = AuthController.currentSessionUser;

        // 3. Якщо юзер знайдений — записуємо його як автора
        if (activeUsername != null && !activeUsername.isEmpty()) {
            discussion.setAuthor(activeUsername);
        } else {
            // Якщо сесія пуста (наприклад, сервер забув після перезапуску), пишемо Анонім
            discussion.setAuthor("Анонім");
        }

        // 4. Зберігаємо в базу даних
        Discussion savedDiscussion = discussionRepository.save(discussion);

        return ResponseEntity.ok(savedDiscussion);
    }

    // Метод для видалення коментаря (для адмінів)
    @DeleteMapping("/{slug}/discussions/{discussionId}")
    public ResponseEntity<?> deleteDiscussion(
            @PathVariable String slug,
            @PathVariable Long discussionId) {

        discussionRepository.deleteById(discussionId);
        return ResponseEntity.ok().build();
    }
}