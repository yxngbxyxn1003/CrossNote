package com.swulion.crossnote.service;

import com.swulion.crossnote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/*
 * JWT 인증 시, 토큰의 email (username)을 기반으로
 * DB에서 UserDetails 객체를 로드하는 서비스
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // email(username)을(를) 기반으로 DB에서 유저 정보를 찾기
        return userRepository.findByEmail(email)
                .map(this::createUserDetails) // 찾으면 UserDetails 객체로 변환
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 유저를 찾을 수 없습니다: " + email));
    }

    // DB의 User 엔티티를 Spring Security의 UserDetails 객체로 변환
    private UserDetails createUserDetails(com.swulion.crossnote.entity.User user) {

        // 소셜 로그인 유저는 password가 null이므로, null 대신 빈 문자열("") 넣음
        String password = (user.getPassword() == null) ? "" : user.getPassword();

        return new User(
                user.getEmail(),
                password, // null이 아닌 빈 문자열이 들어감
                Collections.singletonList(() -> "ROLE_USER")
        );
    }
}