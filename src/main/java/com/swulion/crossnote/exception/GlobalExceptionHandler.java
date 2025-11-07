package com.swulion.crossnote.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/*
 [전역 예외 처리기]
 @RestControllerAdvice: 모든 @RestController에서 발생하는 예외를 가로챔
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     @Valid 유효성 검사 실패 예외 처리 (400 Bad Request)
     (AuthController의 @Valid @RequestBody ... 가 실패했을 때 호출됨)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 에러 반환
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, Object> errors = new HashMap<>();
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Bad Request");

        // 복잡한 에러 메시지 대신, DTO의 @NotBlank(message = "...")에 설정한 default message 가져옴
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        errors.put("message", errorMessage);

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /*
     서비스 로직 상의 예외 처리 (400 Bad Request)
     (UserService의 "비밀번호가 일치하지 않습니다." 등)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 에러 반환
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {

        Map<String, Object> errors = new HashMap<>();
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Bad Request");
        // 서비스에서 던진 에러 메시지를 그대로 사용
        errors.put("message", ex.getMessage());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /*
     그 외 모든 서버 내부 예외 처리 (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500 에러 반환
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {

        Map<String, Object> errors = new HashMap<>();
        errors.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errors.put("error", "Internal Server Error");
        errors.put("message", "서버 내부 오류가 발생했습니다: " + ex.getMessage());

        return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}