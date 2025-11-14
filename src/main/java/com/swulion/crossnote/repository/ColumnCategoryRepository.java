package com.swulion.crossnote.repository;

import com.swulion.crossnote.entity.ColumnCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnCategoryRepository extends JpaRepository<ColumnCategory, Integer> {
    List<ColumnCategory> findByColumnId(Long columnId);
}
