package com.swulion.crossnote.oauth;

import com.swulion.crossnote.entity.User;
import com.swulion.crossnote.repository.UserRepository;
import com.swulion.crossnote.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        log.info("OAuth2 로그인 성공");

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

        // 위에서 넣어준 email 키로 이메일 꺼냄
        String email = (String) oAuth2User.getAttributes().get("email");

        // DB에서 userId 찾아오기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // JWT 생성
        String accessToken = jwtTokenProvider.generateAccessToken(email, user.getUserId());

        // JSON 응답 반환
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"accessToken\": \"" + accessToken + "\"}");
        response.setStatus(HttpServletResponse.SC_OK);

        log.info("JWT 응답 전송 및 세션 정리 완료");
    }
}
