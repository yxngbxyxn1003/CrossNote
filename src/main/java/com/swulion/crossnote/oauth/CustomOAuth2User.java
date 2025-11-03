package com.swulion.crossnote.oauth;

import com.swulion.crossnote.entity.LoginType;
import com.swulion.crossnote.entity.User;
import com.swulion.crossnote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2UserService.loadUser() 실행: 소셜 로그인(유저 정보) 처리 시작");

        try {
            // 1. 기본 OAuth2 유저 서비스
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(userRequest);

            // 2. Provider ID (kakao, google)
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            String userNameAttributeName = userRequest.getClientRegistration()
                    .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

            // 3. 유저 정보(Attributes)
            Map<String, Object> attributes = oAuth2User.getAttributes();

            // 4. DTO(OAuthAttributes)로 유저 정보 통일
            OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, userNameAttributeName, attributes);
            log.info("소셜 유저 정보 파싱 완료. (Email: {})", oAuthAttributes.getEmail());

            // 5. DB에 유저 저장 또는 업데이트
            User user = saveOrUpdateSocialUser(oAuthAttributes);
            log.info("소셜 유저 DB 저장/업데이트 완료. (User ID: {})", user.getUserId());

            // 6. Spring Security의 DefaultOAuth2User 객체 생성 및 반환
            if (user.getEmail() == null) {
                log.error("[치명적 오류] saveOrUpdateSocialUser 후 user.getEmail()이 null입니다.");
                throw new OAuth2AuthenticationException(new OAuth2Error("USER_EMAIL_NULL", "저장된 유저의 이메일이 null입니다.", null));
            }

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    Collections.singletonMap("email", user.getEmail()), // SuccessHandler에서 user를 찾기 위한 Key
                    "email" // NameAttributeKey
            );

        } catch (Exception e) { // 예외 처리
            log.error("CustomOAuth2User.loadUser() 실행 중 예외 발생", e);
            OAuth2Error error = new OAuth2Error("LOAD_USER_FAILED", "OAuth2 로그인 처리 중 예외 발생: " + e.getMessage(), null);
            throw new OAuth2AuthenticationException(error, e);
        }
    }

    private User saveOrUpdateSocialUser(OAuthAttributes attributes) {
        // (try-catch는 상위 loadUser 메서드에서 이미 처리하고 있으므로, 여기서는 핵심 에러만 로깅)

        // 이메일 null 체크
        if (attributes.getEmail() == null) {
            log.error("소셜 로그인 이메일이 null입니다. (Provider: {})", attributes.getLoginType());
            throw new OAuth2AuthenticationException(new OAuth2Error("EMAIL_NOT_PROVIDED", "소셜 로그인 이메일 정보가 없습니다.", null));
        }

        Optional<User> userOptional = userRepository.findByEmail(attributes.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();

            // 로컬 유저 중복 체크
            if (user.getLoginType() == LoginType.LOCAL) {
                log.error("소셜 로그인 시도: 이메일이 로컬 계정과 중복됩니다. (Email: {})", user.getEmail());
                throw new OAuth2AuthenticationException(new OAuth2Error("EMAIL_DUPLICATE_LOCAL", "이미 존재하는 로컬 계정과 이메일이 중복되어 소셜 로그인할 수 없습니다.", null));
            }

            // 기존 소셜 유저면 정보 업데이트
            log.info("기존 소셜 유저 발견. 유저 정보 업데이트. (Email: {})", user.getEmail());
            user = user.updateSocialInfo(attributes.getName(), attributes.getProfileImageUrl());

        } else {
            // 새로 가입하는 소셜 유저
            log.info("신규 소셜 유저 발견. DB에 새로 저장. (Email: {})", attributes.getEmail());
            user = attributes.toEntity();
        }

        return userRepository.save(user);
    }
}