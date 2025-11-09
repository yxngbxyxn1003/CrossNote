package com.swulion.crossnote.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swulion.crossnote.entity.User;
import com.swulion.crossnote.repository.UserRepository;
import com.swulion.crossnote.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
- 동작 방식: 로그인 성공 후 HTTP 응답에 JSON 형태로 JWT 반환
- FE가 아닌 Postman, API 테스트 목적
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider; // JWT 사용 시
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate; // Redis 주입
    private final ObjectMapper objectMapper; // JSON 응답

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        log.info("OAuth2 로그인 성공");

        try {
            DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
            // 위에서 넣어준 email 키로 이메일 꺼냄
            String email = (String) oAuth2User.getAttributes().get("email");

            // DB에서 userId 찾아오기
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found: " + email));

            /* 토큰 생성 및 Redis 저장 */
            // Access Token 생성 (Email, UserId)
            String accessToken = jwtTokenProvider.generateAccessToken(email, user.getUserId());
            // Refresh Token 생성 (Email)
            String refreshToken = jwtTokenProvider.generateRefreshToken(email);

            // Redis에 refresh Token 저장 (Key: email, Value: refreshToken)
            redisTemplate.opsForValue().set(
                    "RT:" + user.getEmail(), refreshToken, // Key, Value
                    jwtTokenProvider.getRefreshTokenExpirationTime(), // 만료 시간
                    TimeUnit.MILLISECONDS //ms 단위
            );

            // JSON 응답에 Access/Refresh 토큰 모두 담기
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            // ObjectMapper를 사용해 Map을 JSON 문자열로 변환
            response.getWriter().write(objectMapper.writeValueAsString(tokens));

            // 세션 및 컨텍스트 정리
            request.getSession().invalidate();
            SecurityContextHolder.clearContext();

            log.info("JWT 응답 전송(Access/Refresh) 및 세션/컨텍스트 정리 완료.");

        } catch (Exception e) { // 예외처리
            log.error("OAuth2 Success Handler 실행 중 예외 발생", e);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"로그인 성공 후 처리 중 오류 발생 했습니다.\"}");
        }
    }
}
