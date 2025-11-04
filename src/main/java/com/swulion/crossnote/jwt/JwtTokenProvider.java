package com.swulion.crossnote.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
/* 토큰 발급기 */
/* 모든 로그인(UserService, OAuth2LoginSuccessHandler)이 JWT를 생성할 때,
   모든 API 요청(JwtAuthenticationFilter, AuthController)이 JWT를 검증/파싱할 때 사용되는 토큰 발급기.
*/
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationTime;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey,
                            @Value("${jwt.expiration-time}") long expirationTime) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationTime = expirationTime;
    }

    /* [JWT Access Token 생성] */
    public String generateAccessToken(String email, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /* [JWT 토큰 유효성 검증] */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다: {}", token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT 토큰입니다: {}", token);
        }
        return false;
    }

    /* [토큰에서 Claims 추출] */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /* [토큰에서 이메일(Subject) 추출] */
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    /* [토큰에서 userId(Custom Claim) 추출] */
    public Long getUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }
}

