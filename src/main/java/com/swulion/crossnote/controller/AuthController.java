package com.swulion.crossnote.controller;

import com.swulion.crossnote.dto.RefreshTokenRequestDto;
import com.swulion.crossnote.jwt.JwtBlacklistService;
import com.swulion.crossnote.service.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import com.swulion.crossnote.jwt.JwtTokenProvider;
import com.swulion.crossnote.repository.UserRepository;
import com.swulion.crossnote.entity.User;
import io.jsonwebtoken.ExpiredJwtException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.swulion.crossnote.service.UserService;

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
    private final JwtTokenProvider jwtTokenProvider; // 토큰 파싱
    private final UserRepository userRepository; // DB 조회
    private final RedisTemplate<String, Object> redisTemplate; // Redis 주입

    /*
     JWT 로그아웃 엔드포인트
     [POST] /auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        log.info("로그아웃 요청 수신...");

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("{\"error\": \"유효한 토큰이 헤더에 없습니다.\"}");
        }

        // "Bearer " 부분을 제외한 순수 Access Token을 추출
        final String accessToken = authHeader.substring(7);

        try {
            // 1. Access Token에서 이메일 추출
            String email = jwtTokenProvider.getEmail(accessToken);

            // 2. Redis에서 Refresh Token 삭제
            // (로그아웃 시, Access Token과 Refresh Token을 모두 무효화)
            String redisKey = "RT:" + email;
            if (redisTemplate.opsForValue().get(redisKey) != null) {
                redisTemplate.delete(redisKey); // Refresh Token 삭제
                log.info("Redis에서 Refresh Token 삭제 완료. (User: {})", email);
            }

            // 3. Access Token을 블랙리스트에 추가
            jwtBlacklistService.blacklistToken(accessToken);
            log.info("Access Token이 블랙리스트에 추가되었습니다. (User: {})", email);

            // 4. SecurityContext 비우기
            SecurityContextHolder.clearContext();

            // 5. 로그아웃 메시지 생성
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("토큰에 해당하는 유저를 찾을 수 없습니다."));
            String loginTypeStr = user.getLoginType().toString();
            String message = String.format("{\"message\": \"%s: %s 님이 로그아웃 되었습니다.\"}", loginTypeStr, email);

            return ResponseEntity.ok(message);

        } catch (ExpiredJwtException e) {
            log.warn("이미 만료된 Access Token으로 로그아웃을 시도했습니다.");
            // 만료된 토큰이어도 블랙리스트엔 추가 (재사용 방지)
            jwtBlacklistService.blacklistToken(accessToken);
            return ResponseEntity.status(401).body("{\"error\": \"이미 만료된 토큰입니다. 로그아웃 처리(블랙리스트) 되었습니다.\"}");
        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("{\"error\": \"로그아웃 처리 중 오류가 발생했습니다: " + e.getMessage() + "\"}");
        }
    }

    /*
     Access Token 재발급
     [POST] /auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshTokekn(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto){
        final String refreshToken = refreshTokenRequestDto.getRefreshToken();
        log.info("Access Token 재발급 요청 수신...");

        try {
            // 1. Refresh Token 유효성 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                log.warn("유효하지 않은 Refresh Token입니다.");
                throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
            }

            // 2. 토큰에서 이메일 추출
            String email = jwtTokenProvider.getEmail(refreshToken);

            // 3. Redis에 저장된 토큰과 일치하는지 확인
            String redisKey = "RT:" + email;
            String redisRefreshToken = (String)redisTemplate.opsForValue().get(redisKey);

            if(redisRefreshToken == null || !redisRefreshToken.equals(refreshToken)){
                log.warn("Redis에 저장된 토큰과 일치하지 않습니다.");
                throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
            }

            // 4. DB에서 유저 정보 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Refresh Token에 해당하는 유저를 찾을 수 없습니다."));

            // 5. 새로운 Access Token 발급
            String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getUserId());
            log.info("Access Token 재발급 성공. (User: {})", email);

            // 6. 새 Access Token을 JSON으로 변환
            Map<String, String> response = new HashMap<>();
            response.put("access_token", newAccessToken);

            return ResponseEntity.ok(response);

        } catch (ExpiredJwtException e) {
            // Refresh Token이 만료된 경우
            log.warn("만료된 Refresh Token으로 재발급을 시도했습니다.");
            return ResponseEntity.status(401).body("{\"error\": \"Refresh Token이 만료되었습니다. 다시 로그인해주세요.\"}");
        } catch (Exception e) {
            // 그 외 (토큰 형식 오류, DB 조회 실패 등)
            log.error("토큰 재발급 중 오류 발생", e);
            return ResponseEntity.status(401).body("{\"error\": \"토큰 재발급에 실패했습니다: " + e.getMessage() + "\"}");
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