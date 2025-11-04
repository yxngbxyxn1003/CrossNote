package com.swulion.crossnote.config;

import com.swulion.crossnote.oauth.CustomOAuth2User;
//import com.swulion.crossnote.oauth.OAuth2AuthenticationSuccessHandler;
import com.swulion.crossnote.oauth.OAuth2LoginSuccessHandler;
import com.swulion.crossnote.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

/* Spring Security 설정 */
@Configuration
@EnableWebSecurity // Spring Security 설정 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    // JWT 인증 필터
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomOAuth2User customOAuth2UserService;
    //private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler; // FE 연결용
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; // BE 테스트용

    public static final String[] allowUrls = {
            "/health",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/login"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 'SecurityFilterChain' 빈을 추가하여 HTTP 보안 설정을 구성
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 기능 비활성화
                .csrf(csrf -> csrf.disable())

                // [CORS 설정 추가]
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 관리
                // IF_REQUIRED는 OAuth2 로그인 시에는 세션을 사용하지만 JWT 인증 시에는 사용하지 않도록 함
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // 기본 폼 로그인과 HTTP Basic 인증을 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // API 엔드포인트별 접근 권한을 설정
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // 로그인/로그아웃 관련은 모두 허용
                                .requestMatchers("/auth/logout", "/auth/local/**", "/auth/login/**").permitAll()
                                .requestMatchers(allowUrls).permitAll()  // 허용 URL 설정
                                // 그 외 모든 요청은 인증 필요
                                .anyRequest().authenticated()
                )

                // JWT 필터
                // Spring Security 필터 체인의 가장 앞단에 배치하여, 모든 요청을 토큰 검사부터 하도록 설정
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // OAuth2 로그인 설정
                // 소셜 로그인(OAuth2) 기능 활성화하고, 관련 서비스(CustomOAuth2User, OAuth2LoginSuccessHandler)를(을) 연결
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        //.successHandler(oAuth2AuthenticationSuccessHandler) // FE 테스트용
                        .successHandler(oAuth2LoginSuccessHandler) // BE 테스트용
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/auth/login") // 카카오/구글 로그인 시작 주소
                        )
                )
                // HTML 리다이렉트 대신 JSON 형태로 인증 실패 응답
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\": \"인증이 필요합니다.\"}");
                        })
                );

        return http.build();
    }

    // [CORS 설정 Bean 추가]
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOrigin("http://localhost:3000"); // 로컬 프론트엔드
        // config.addAllowedOrigin("https://frontend-domain.com"); // 배포할 프론트엔드 도메인

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 적용
        return source;
    }
}
