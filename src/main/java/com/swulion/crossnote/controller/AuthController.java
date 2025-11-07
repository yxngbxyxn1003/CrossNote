package com.swulion.crossnote.controller;

import com.swulion.crossnote.jwt.JwtBlacklistService;
import com.swulion.crossnote.service.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swulion.crossnote.jwt.JwtTokenProvider;
import com.swulion.crossnote.repository.UserRepository;
import com.swulion.crossnote.entity.User;
import io.jsonwebtoken.ExpiredJwtException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.swulion.crossnote.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;


/*
 * 인증 관련(로그아웃, 내 정보 조회 등) 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/auth") // 공통 경로
@RequiredArgsConstructor
public class AuthController {

    private final JwtBlacklistService jwtBlacklistService;

    // 토큰 파싱과 DB 조회를 위한 의존성 주입
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /*
     JWT 로그아웃 엔드포인트
     [POST] /auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        log.info("로그아웃 요청 수신...");

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("Authorization 헤더가 없거나 유효하지 않아 로그아웃을 처리할 수 없습니다.");
            return ResponseEntity.badRequest().body("{\"error\": \"유효한 토큰이 헤더에 없습니다.\"}");
        }

        // "Bearer " 부분을 제외한 순수 토큰을 추출
        final String token = authHeader.substring(7);

        try {
            // 1. 토큰에서 이메일을 추출합 (이 과정에서 토큰 유효성 검증도 겸함)
            String email = jwtTokenProvider.getEmail(token);

            // 2. 이메일로 DB에서 유저를 조회하여 LoginType을 가져옴
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("토큰에 해당하는 유저를 찾을 수 없습니다."));

            String loginTypeStr = user.getLoginType().toString(); // (e.g., "KAKAO", "LOCAL", "GOOGLE")

            // 3. 토큰을 블랙리스트에 추가
            jwtBlacklistService.blacklistToken(token);
            log.info("토큰이 블랙리스트에 추가되었습니다. (User: {})", email);

            // 4. 현재 SecurityContext를 비움
            SecurityContextHolder.clearContext();

            // 5. 커스텀 메시지를 생성하여 반환
            String message = String.format("{\"message\": \"%s: %s 님이 로그아웃 되었습니다.\"}", loginTypeStr, email);
            return ResponseEntity.ok(message);

        } catch (ExpiredJwtException e) {
            // 만약 이미 만료된 토큰으로 로그아웃을 시도한 경우
            log.warn("이미 만료된 토큰으로 로그아웃을 시도했습니다. (Token: {})", token);
            return ResponseEntity.status(401).body("{\"error\": \"이미 만료된 토큰입니다.\"}");
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("로그아웃 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("{\"error\": \"로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }

    /*
     내 정보 조회
     [GET] /auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails == null) {
            // 이 로그가 찍히면 필터가 제대로 동작 안 한 것
            log.warn("인증된 사용자 정보를 찾을 수 없습니다.");
            return ResponseEntity.status(401).body("인증 정보가 없습니다.");
        }

        // DB 추가 조회 없이 객체에서 바로 꺼냄
        String userEmail = customUserDetails.getUsername(); // (CustomUserDetails.getEmail() 호출)
        String userName = customUserDetails.getUser().getName(); // (CustomUserDetails.getUser() 호출)
        log.info("인증된 사용자 정보 조회: (이메일: {}, 이름: {})", userEmail, userName);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("email", userEmail);
        responseBody.put("name", userName);

        return ResponseEntity.ok(responseBody);
    }
}