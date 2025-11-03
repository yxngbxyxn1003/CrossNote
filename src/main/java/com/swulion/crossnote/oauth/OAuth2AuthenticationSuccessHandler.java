//package com.swulion.crossnote.oauth;
//
//import com.swulion.crossnote.entity.User;
//import com.swulion.crossnote.jwt.JwtTokenProvider;
//import com.swulion.crossnote.repository.UserRepository;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.io.IOException;
//
///*
//※ 현재 사용하지 않는 코드입니다. 추후 FE와의 연결을 위해 주석 처리해 두었습니다. ※
//- 동작 방식: 로그인 성공 후 프론트엔드 URL로 리다이렉트
//- JWT를 URL 파라미터로 전달
//- FE가 존재하고, 브라우저 리다이렉트 방식으로 OAuth2 토큰 전달할 때
//*/
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final JwtTokenProvider jwtTokenProvider;
//    private final UserRepository userRepository;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws IOException {
//
//        // 1. CustomOAuth2UserService에서 반환한 OAuth2User 객체를 가져옴
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//
//        // 2. CustomOAuth2UserService에서 Map의 Key로 "email"을 사용
//        String email = (String) oAuth2User.getAttributes().get("email");
//
//        log.info("OAuth2 로그인 성공. 이메일: {}", email);
//
//        // 3. 이메일로 DB에서 유저를 찾기
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 유저가 DB에 없습니다. email=" + email));
//
//        // 4. 서비스의 JWT Access Token을 생성
//        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getUserId());
//
//        log.info("JWT Access Token 생성 완료: {}", accessToken);
//
//        // 5. 프론트엔드로 리다이렉트할 URL 생성
//        // (CORS 설정에 등록된 http://localhost:3000으로 전송)
//        String targetUrl = "http://localhost:3000/oauth-redirect"; // 프론트엔드에서 이 주소를 받을 라우터
//
//        // 6. URL 파라미터에 Access Token을 추가
//        String redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
//                .queryParam("token", accessToken) // (프론트엔드와 "token"이라는 이름으로 맞춤)
//                .build().toUriString();
//
//        // 7. 응답(response)을 통해 클라이언트(브라우저)를 해당 URL로 리다이렉트
//        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
//    }
//}