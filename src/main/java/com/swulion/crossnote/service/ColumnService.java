package com.swulion.crossnote.service;

import com.swulion.crossnote.dto.ColumnListDto;
import com.swulion.crossnote.dto.ColumnReadResponseDto;
import com.swulion.crossnote.dto.ColumnRequestDto;
import com.swulion.crossnote.dto.ColumnDetailResponseDto;
import com.swulion.crossnote.entity.Category;
import com.swulion.crossnote.entity.ColumnCategory;
import com.swulion.crossnote.entity.ColumnEntity;
import com.swulion.crossnote.entity.User;
import com.swulion.crossnote.repository.CategoryRepository;
import com.swulion.crossnote.repository.ColumnCategoryRepository;
import com.swulion.crossnote.repository.ColumnRepository;
import com.swulion.crossnote.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ColumnService {
    private final ColumnRepository columnRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ColumnCategoryRepository columnCategoryRepository;

    /* 칼럼 피드 */
//    public List<ColumnListDto> getColumnHome(){
//        List<ColumnEntity> columns = columnRepository.findAllOrderByCreateDateDesc();
//        List<ColumnListDto> columnListDtos = new ArrayList<>();
//        for (ColumnEntity columnEntity : columns) {
//        columnListDtos.add(new ColumnListDto(
//                columnEntity.getColumnId(),
//                columnEntity.getColumnAutherId(),
//                columnEntity.getTitle(),
//                columnEntity.getContent(),
//                columnEntity.getImageUrl(),
//                columnEntity.getLikeCount(),
//                columnEntity.getCommentCount()
//        ));}
//        return columnListDtos;
//    }

    /* 칼럼 생성 */
    public ColumnDetailResponseDto createColumn(ColumnRequestDto columnRequestDto, Long loginUserId) {

        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        ColumnEntity columnEntity = new ColumnEntity();
        columnEntity.setCreatedAt(LocalDateTime.now());
        columnEntity.setUpdatedAt(LocalDateTime.now());
        columnEntity.setLikeCount(0);
        columnEntity.setTitle(columnRequestDto.getTitle());
        columnEntity.setContent(columnRequestDto.getContent());
        columnEntity.setImageUrl(columnRequestDto.getImageUrl());

        User columnAuthorId = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        columnEntity.setColumnAutherId(columnAuthorId);
        columnRepository.save(columnEntity);

        List<Long> categories = getCategories(columnEntity, columnRequestDto);

        return new ColumnDetailResponseDto(
                columnEntity.getColumnAutherId().getUserId(),
                columnEntity.getTitle(),
                columnEntity.getContent(),
                columnEntity.getLikeCount(),
                0,
                columnEntity.isBestColumn(),
                columnEntity.getImageUrl(),
                columnEntity.getCreatedAt(),
                columnEntity.getUpdatedAt(),
                categories.get(0),
                categories.get(1),
                categories.get(2)

        );


    }

    /* 칼럼 삭제 */
    public Integer deleteColumn(Long columnId) {
        ColumnEntity columnEntity = columnRepository.findById(columnId).orElse(null);

        if (columnEntity == null){
            throw new EntityNotFoundException("Column Not Found");
        }else {

            columnCategoryRepository.deleteByColumnId(columnEntity);
            columnRepository.delete(columnEntity);
            return 1;
        }

    }

    /* 칼럼 수정 */
    public ColumnDetailResponseDto updateColumn(Long columnId, ColumnRequestDto columnRequestDto, Long loginUserId) {

        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        ColumnEntity columnEntity = columnRepository.findById(columnId).orElse(null);
        if (columnEntity == null){
            throw new EntityNotFoundException("Column Not Found");
        }else {
            columnEntity.setTitle(columnRequestDto.getTitle());
            columnEntity.setContent(columnRequestDto.getContent());
            columnEntity.setImageUrl(columnRequestDto.getImageUrl());
            columnEntity.setUpdatedAt(LocalDateTime.now());
            columnRepository.save(columnEntity);
        }

        List<Long> categories = getCategories(columnEntity, columnRequestDto);

        return new ColumnDetailResponseDto(
                user.getUserId(),
                columnEntity.getTitle(),
                columnEntity.getContent(),
                columnEntity.getLikeCount(),
                columnEntity.getCommentCount(),
                columnEntity.isBestColumn(),
                columnEntity.getImageUrl(),
                columnEntity.getCreatedAt(),
                columnEntity.getUpdatedAt(),
                categories.get(0),
                categories.get(1),
                categories.get(2)

        );
    }

    /* 전체 칼럼 조회 */
    public List<ColumnReadResponseDto> getColumnHome() {
        List<ColumnEntity> columnEntities = columnRepository.findAllByOrderByCreatedAtDesc();
        List<ColumnReadResponseDto> columnReadResponseDtos = new ArrayList<>();
        for (ColumnEntity columnEntity : columnEntities) {
            ColumnReadResponseDto columnReadResponseDto = new ColumnReadResponseDto();
            columnReadResponseDto.setColumnId(columnEntity.getColumnId());

            List<ColumnCategory> columnCategories = columnCategoryRepository.findByColumnId(columnEntity);
            List<Long> categories = new ArrayList<>();
            for (ColumnCategory columnCategory : columnCategories) {
                Category category = columnCategory.getCategoryId();
                categories.add(category.getCategoryId());
            }

            Long cat2 = categories.size() > 1 ? categories.get(1) : null;
            Long cat3 = categories.size() > 2 ? categories.get(2) : null;

            columnReadResponseDto.setAuthorId(columnEntity.getColumnAutherId().getUserId());
            columnReadResponseDto.setTitle(columnEntity.getTitle());
            columnReadResponseDto.setIsBestColumn(columnEntity.isBestColumn());
            columnReadResponseDto.setCommentCount(columnEntity.getCommentCount());
            columnReadResponseDto.setLikeCount(columnEntity.getLikeCount());
            columnReadResponseDto.setCategoryId1(categories.get(0));
            columnReadResponseDto.setCategoryId2(cat2);
            columnReadResponseDto.setCategoryId3(cat3);
            columnReadResponseDtos.add(columnReadResponseDto);
        }
        return columnReadResponseDtos;

    }

    public ColumnDetailResponseDto getColumn(Long columnId) {
        ColumnEntity columnEntity = columnRepository.findById(columnId).orElse(null);
        if (columnEntity == null){
            throw new EntityNotFoundException("Column Not Found");
        }else {
            List<ColumnCategory> columnCategories = columnCategoryRepository.findByColumnId(columnEntity);
            List<Long> categories = new ArrayList<>();
            for (ColumnCategory columnCategory : columnCategories) {
                Category category = columnCategory.getCategoryId();
                categories.add(category.getCategoryId());
            }

            Long cat2 = categories.size() > 1 ? categories.get(1) : null;
            Long cat3 = categories.size() > 2 ? categories.get(2) : null;

            return new ColumnDetailResponseDto(
                    columnEntity.getColumnAutherId().getUserId(),
                    columnEntity.getTitle(),
                    columnEntity.getContent(),
                    columnEntity.getLikeCount(),
                    columnEntity.getCommentCount(),
                    columnEntity.isBestColumn(),
                    columnEntity.getImageUrl(),
                    columnEntity.getCreatedAt(),
                    columnEntity.getUpdatedAt(),
                    categories.get(0),
                    cat2,
                    cat3
            );
        }

    }


    /* 카테고리 추출 method */
    private List<Long> getCategories(ColumnEntity columnEntity, ColumnRequestDto columnRequestDto) {
        Category category1 = categoryRepository.findByCategoryId(columnRequestDto.getCategory1());
        Category category2 = categoryRepository.findByCategoryId(columnRequestDto.getCategory2());
        Category category3 = categoryRepository.findByCategoryId(columnRequestDto.getCategory3());

        ColumnCategory columnCategory1 = new ColumnCategory();
        columnCategory1.setCategoryId(category1);
        columnCategory1.setColumnId(columnEntity);
        columnCategory1.setCreatedAt(LocalDateTime.now());
        columnCategoryRepository.save(columnCategory1);

        ColumnCategory columnCategory2 = null;
        ColumnCategory columnCategory3 = null;
        if(category2 != null){
            columnCategory2 = new ColumnCategory();
            columnCategory2.setCategoryId(category2);
            columnCategory2.setColumnId(columnEntity);
            columnCategory2.setCreatedAt(LocalDateTime.now());
            columnCategoryRepository.save(columnCategory2);
        }
        if(category3 != null){
            columnCategory3 = new ColumnCategory();
            columnCategory3.setCategoryId(category3);
            columnCategory3.setColumnId(columnEntity);
            columnCategory3.setCreatedAt(LocalDateTime.now());
            columnCategoryRepository.save(columnCategory3);
        }

        Long responseCatId2 = (category2 != null) ? category2.getCategoryId() : null;
        Long responseCatId3 = (category3 != null) ? category3.getCategoryId() : null;

        List<Long> categories = new ArrayList<>();
        categories.add(category1.getCategoryId());
        categories.add(responseCatId2);
        categories.add(responseCatId3);

        return categories;
    }
}
