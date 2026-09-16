package com.ajthapa.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByAppUserId(Long userId);
    Optional<Category> findByIdAndAppUserId(Long id, Long userId);
}
