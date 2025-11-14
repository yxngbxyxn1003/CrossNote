package com.swulion.crossnote.repository;

import com.swulion.crossnote.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategoryName(String categoryName);
    Category findByCategoryId(Long categoryId);

    // 세부 카테고리 조회용
    List<Category> findByParentCategoryId(Category parent);
}
