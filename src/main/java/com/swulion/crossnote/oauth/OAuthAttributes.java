package com.swulion.crossnote.oauth;

import com.swulion.crossnote.entity.LoginType;
import com.swulion.crossnote.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Getter
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String profileImageUrl;
    private LocalDate birthDate;
    private LoginType loginType;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey,
                           String name, String email, String profileImageUrl,
                           LocalDate birthDate, LoginType loginType) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.birthDate = birthDate;
        this.loginType = loginType;
    }

    // Provider(소셜)별로 유저 정보를 변환
    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                     Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        }
        if ("google".equals(registrationId)) {
            return ofGoogle(userNameAttributeName, attributes);
        }
        throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    // [Kakao] 유저 정보(attributes)를 OAuthAttributes로 변환
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        // [방어 코드 1] "profile"이 null일 수 있으므로 (동의 안 하면) null 체크
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        String email = (String) kakaoAccount.get("email");
        String name = (String) kakaoAccount.get("name");

        // [방어 코드 2] kakaoProfile이 null이 아닐 때만 이미지 URL을 가져옴
        String profileImageUrl = null;
        if (kakaoProfile != null) {
            profileImageUrl = (String) kakaoProfile.get("profile_image_url");
        }

        // 생년월일 파싱 (AuthService 로직 참고)
        String birthyear = (String) kakaoAccount.get("birthyear"); // YYYY
        String birthday = (String) kakaoAccount.get("birthday"); // MMDD
        LocalDate parsedBirthDate = parseBirthDate(birthyear, birthday);

        return OAuthAttributes.builder()
                .name(name)
                .email(email)
                .profileImageUrl(profileImageUrl) // null일 수도 있음
                .birthDate(parsedBirthDate) // 파싱된 생년월일
                .loginType(LoginType.KAKAO) // Provider 타입 설정
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // [Google] 유저 정보(attributes)를 OAuthAttributes로 변환
    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        // Google은 최상위에 유저 정보가 있습니다.
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String profileImageUrl = (String) attributes.get("picture");

        return OAuthAttributes.builder()
                .name(name)
                .email(email)
                .profileImageUrl(profileImageUrl)
                .birthDate(null) // 구글은 기본 scope로 생년월일 안 줌
                .loginType(LoginType.GOOGLE) // Provider 타입 설정
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // 생년월일 파싱 헬퍼 메서드
    private static LocalDate parseBirthDate(String birthyear, String birthday) {
        try {
            if (birthyear != null && birthyear.matches("\\d{4}") &&
                    birthday != null && birthday.matches("\\d{4}")) {
                int year = Integer.parseInt(birthyear);
                int month = Integer.parseInt(birthday.substring(0, 2));
                int day = Integer.parseInt(birthday.substring(2));

                if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                    return LocalDate.of(year, month, day);
                }
            }
        } catch (Exception e) {
            log.warn("소셜 로그인 생년월일 파싱 실패: {} {}", birthyear, birthday, e);
        }
        return null;
    }

    // OAuthAttributes 정보를 기반으로 User 엔티티 생성 (신규 회원가입 시)
    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .profileImageUrl(profileImageUrl)
                .birthDate(birthDate) // 생년월일 추가
                .loginType(loginType) // KAKAO 또는 GOOGLE
                .curationLevel(null)
                // 소셜 로그인은 password가 필요 없으므로 null
                .build();
    }
}

