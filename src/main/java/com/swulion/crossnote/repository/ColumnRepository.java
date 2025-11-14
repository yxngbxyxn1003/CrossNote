package com.swulion.crossnote.repository;

import com.swulion.crossnote.entity.ColumnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnRepository extends JpaRepository<ColumnEntity, Long> {
    List<ColumnEntity> findAllByColumnAuthorId(Long columnAuthorId);
    List<ColumnEntity> findByAllColumnTitle(String columnTitle);
    //최신순
    List<ColumnEntity> findAllOrderByCreateDateDesc();
    //댓글많은 순
    List<ColumnEntity> findAllOrderByCommentCountDesc();
}
