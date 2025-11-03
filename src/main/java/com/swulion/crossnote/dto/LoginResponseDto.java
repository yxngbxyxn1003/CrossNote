package com.swulion.crossnote.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {

    /*
     * JWT Access Token
     * - 로컬 로그인 및 소셜 로그인(카카오, 구글 등) 시 발급
     * - 프론트에서는 Authorization: Bearer {accessToken} 형식으로 사용
     */
    private final String accessToken;

    /*
     * 토큰 타입
     * - 항상 "Bearer"
     * - HTTP Authorization 헤더 사용 시 필요
     */
    private final String tokenType = "Bearer";

    /*
     * 생성자
     * @param accessToken JWT Access Token
     *
     * 로컬과 소셜 로그인 통합 관리용 DTO
     * - 두 로그인 방식을 하나의 Response DTO로 통일
     * - 프론트 측에서는 로그인 타입(로컬/소셜) 구분 없이 accessToken 사용 가능
     */
    public LoginResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }
}
