package com.swulion.crossnote.controller;

import com.swulion.crossnote.dto.LocalLoginRequestDto;
import com.swulion.crossnote.dto.LocalSignUpRequestDto;
import com.swulion.crossnote.dto.LoginResponseDto;
import com.swulion.crossnote.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/local")
public class UserController {

    private final UserService userService;

    /*
     로컬 회원가입 API
     [POST] /auth/local/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<String> localSignUp(@Valid @RequestBody LocalSignUpRequestDto requestDto) {
        // 서비스 로직 호출 (회원가입)
        Long newUserId = userService.registerLocalUser(requestDto);

        // 회원가입 성공 응답
        return ResponseEntity.ok("회원가입에 성공했습니다. (유저 ID: " + newUserId + ")");
    }

    /*
    로컬 로그인 API
    [POST] /auth/local/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> localLogin(@Valid @RequestBody LocalLoginRequestDto requestDto) {
        // 로그인 성공 시 Access Token이 담긴 DTO를 받음
        LoginResponseDto responseDto = userService.login(requestDto);

        // 로그인 성공 시, 200 OK와 함께 JWT 토큰이 담긴 DTO를 JSON으로 반환
        return ResponseEntity.ok(responseDto);
    }

    /* 로컬 로그아웃은 AuthController의 /auth/logout API를 공통으로 사용함 */
}

