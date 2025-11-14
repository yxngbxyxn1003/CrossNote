package com.swulion.crossnote.controller;

import com.swulion.crossnote.dto.ColumnListDto;
import com.swulion.crossnote.dto.ColumnReadResponseDto;
import com.swulion.crossnote.dto.ColumnRequestDto;
import com.swulion.crossnote.dto.ColumnDetailResponseDto;
import com.swulion.crossnote.service.ColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/column")
public class ColumnController {

    private final ColumnService columnService;

    /* Column 홈 */
    @GetMapping("/home")
    public ResponseEntity<List<ColumnReadResponseDto>> getColumnHome() {
        List<ColumnReadResponseDto> columnReadResponseDtos = columnService.getColumnHome();
        return ResponseEntity.ok(columnReadResponseDtos);
    }

    /* Column 생성 */
    @PostMapping("/create")
    public ResponseEntity<ColumnDetailResponseDto> createColumn(@RequestBody ColumnRequestDto columnRequestDto){
        ColumnDetailResponseDto columnDetailResponseDto = columnService.createColumn(columnRequestDto);
        return ResponseEntity.ok(columnDetailResponseDto);
    }

    /* Column 삭제 */
    @DeleteMapping("/delete")
    public ResponseEntity<Integer> deleteColumn(@PathVariable Long columnId){
        Integer value = columnService.deleteColumn(columnId);
        return ResponseEntity.ok(value);
    }

    /* Column 상세 보기 */
    @GetMapping("/detail")
    public ResponseEntity<ColumnDetailResponseDto> getColumnDetail(@PathVariable Long columnId){
        ColumnDetailResponseDto columnDetailResponseDto = columnService.getColumn(columnId);
        return ResponseEntity.ok(columnDetailResponseDto);
    }

    /* Column 수정 */
    @PostMapping("/update")
    public ResponseEntity<ColumnDetailResponseDto> updateColumn(@PathVariable Long columnId, @RequestBody ColumnRequestDto columnRequestDto){
        ColumnDetailResponseDto columnDetailResponseDto = columnService.updateColumn(columnId, columnRequestDto);
        return ResponseEntity.ok(columnDetailResponseDto);
    }
}
