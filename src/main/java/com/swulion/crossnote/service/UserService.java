package com.swulion.crossnote.service;

import com.swulion.crossnote.dto.LocalLoginRequestDto;
import com.swulion.crossnote.dto.LocalSignUpRequestDto;
import com.swulion.crossnote.dto.LoginResponseDto;
import com.swulion.crossnote.entity.LoginType;
import com.swulion.crossnote.entity.User;
import com.swulion.crossnote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.swulion.crossnote.jwt.JwtTokenProvider;


@Service
@RequiredArgsConstructor
@Transactional
/* 로컬 회원 로직 */
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /* 로컬 회원가입 로직 */
    @Transactional
    public Long registerLocalUser(LocalSignUpRequestDto requestDto) {

        // 비밀번호 일치 여부 확인
        if (!requestDto.getPassword().equals(requestDto.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }

        // 이메일 중복 확인
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // User Entity 생성
        User newUser = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword) // 암호화된 비밀번호 저장
                .name(requestDto.getName())
                .loginType(LoginType.LOCAL) // 로컬 타입
                .curationLevel(1) // 기본값 1로 설정
                .build();

        // DB에 저장
        User savedUser = userRepository.save(newUser);

        return savedUser.getUserId();
    }

    /* 로컬 로그인 로직 */
    @Transactional(readOnly = true)
    public LoginResponseDto login(LocalLoginRequestDto requestDto) {


        // 이메일로 유저 찾기 (없으면 예외)
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 유저가 소셜 로그인 유저인지 확인
        if (user.getPassword() == null || user.getLoginType() != LoginType.LOCAL) {
            throw new IllegalArgumentException("소셜 로그인 유저입니다. 소셜 로그인을 이용해주세요.");
        }

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            // matches(평문 비밀번호, 암호화된 비밀번호)
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 일치 -> JWT Access Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getUserId());

        // Access Token을 DTO에 담아 반환
        return new LoginResponseDto(accessToken);
    }
}
