package com.swulion.crossnote.repository;

import com.swulion.crossnote.entity.ColumnCategory;
import com.swulion.crossnote.entity.ColumnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnCategoryRepository extends JpaRepository<ColumnCategory, Long> {
    List<ColumnCategory> findByColumnId(ColumnEntity columnId);

    void deleteByColumnId(ColumnEntity columnId);
}
