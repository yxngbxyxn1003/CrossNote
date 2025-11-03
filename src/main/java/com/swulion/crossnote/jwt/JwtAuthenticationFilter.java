package com.swulion.crossnote.jwt;

import com.swulion.crossnote.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 0. 로그아웃 요청은 필터에서 패스 (만료된 토큰이어도 로그아웃 가능)
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/auth/logout")) {
            log.debug("JWT 필터 패스됨: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 1. 요청 헤더에서 "Authorization" (Bearer 토큰)을 가져옴
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            // 토큰이 없으면 다음 필터로 즉시 넘김
            filterChain.doFilter(request, response);
            return;
        }

        // 2. "Bearer " 부분을 제외한 순수 토큰을 추출
        final String token = authHeader.substring(7);

        try {
            // 3. 토큰이 블랙리스트에 있는지 확인
            if (jwtBlacklistService.isTokenBlacklisted(token)) {
                log.warn("로그아웃된 토큰(블랙리스트)으로 접근 시도: {}", token);response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401 Unauthorized
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\": \"로그아웃된 토큰입니다.\"}");
                return; // 필터 체인 중단
            }

            // 4. 토큰 유효성을 검증 (JwtTokenProvider.validateToken() 호출)
            if (jwtTokenProvider.validateToken(token)) {

                // 5. 토큰에서 이메일(Subject)을 추출 (JwtTokenProvider.getEmail() 호출)
                String email = jwtTokenProvider.getEmail(token);

                // 6. SecurityContext에 인증 정보가 없는 경우에만 DB에서 유저 정보를 가져옴
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 7. email로 UserDetailsService를 통해 UserDetails를 로드
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                    // 8. 인증 토큰(UsernamePasswordAuthenticationToken)을 생성
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null, // (비밀번호는 이미 인증되었으므로 null)
                                    userDetails.getAuthorities()
                            );

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 9. SecurityContextHolder에 인증 정보를 저장 (= 인증 완료 의미)
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

        } catch (Exception e) {
            // (validateToken에서 발생하는 예외 등)
            log.warn("JWT 필터 처리 중 예외 발생: {}", e.getMessage());
            SecurityContextHolder.clearContext(); // 컨텍스트 정리
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401 Unauthorized
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"유효하지 않은 토큰입니다.\"}");
            return;
        }

        // 10. 다음 필터로 요청을 전달
        filterChain.doFilter(request, response);
    }
}