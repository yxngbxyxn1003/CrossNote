package com.swulion.crossnote.repository;

import com.swulion.crossnote.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategoryName(String categoryName);
    Category findByCategoryId(Long categoryId);
}
