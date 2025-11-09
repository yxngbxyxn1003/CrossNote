package com.swulion.crossnote.service;

import com.swulion.crossnote.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/* Spring Security 인증 컨텍스트에 저장될 사용자 정보 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ROLE_USER 기본 권한 부여
        return Collections.singletonList(() -> "ROLE_USER");
    }

    @Override
    public String getPassword() {
        // 소셜 로그인은 password가 null일 수 있으므로, null 대신 빈 문자열 반환
        return user.getPassword() == null ? "" : user.getPassword();
    }

    @Override
    public String getUsername() {
        // Spring Security에서 username을 email로 사용
        return user.getEmail();
    }

    /* 이하 계정 상태 관련 메서드는 모두 true로 반환 */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
