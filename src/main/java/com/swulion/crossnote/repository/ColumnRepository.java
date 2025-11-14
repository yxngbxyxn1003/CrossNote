package com.swulion.crossnote.repository;

import com.swulion.crossnote.entity.ColumnEntity;
import com.swulion.crossnote.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColumnRepository extends JpaRepository<ColumnEntity, Long> {
    List<ColumnEntity> findAllByColumnAutherId(User columnAuthorId);

    List<ColumnEntity> findAllByTitleContaining(String title);
    //최신순
    List<ColumnEntity> findAllByOrderByCreatedAtDesc();
    //댓글많은 순
    List<ColumnEntity> findAllByOrderByCommentCountDesc();
}
