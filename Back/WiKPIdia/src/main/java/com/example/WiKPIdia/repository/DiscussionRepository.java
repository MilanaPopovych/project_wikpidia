package com.example.WiKPIdia.repository;

import com.example.WiKPIdia.entity.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
    // Змінено з List на Page та додано Pageable
    Page<Discussion> findByArticleSlugOrderByCreatedAtDesc(String articleSlug, Pageable pageable);

    Page<Discussion> findByAuthorOrderByIdDesc(String author, Pageable pageable);
}