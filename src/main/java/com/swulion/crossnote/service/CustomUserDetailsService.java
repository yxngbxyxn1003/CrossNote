package com.swulion.crossnote.service;

import com.swulion.crossnote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
 JWT 인증 시, 토큰의 email (username)을 기반으로
 DB에서 UserDetails 객체를 로드하는 서비스
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // email(username)을(를) 기반으로 DB에서 유저 정보를 찾기
        return userRepository.findByEmail(email)
                // CustomUserDetails 객체로 변환
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 유저를 찾을 수 없습니다: " + email));
    }

    /* createUserDetails 메서드는 CustomerUserDetails 클래스가 대체하므로 삭제 */
}