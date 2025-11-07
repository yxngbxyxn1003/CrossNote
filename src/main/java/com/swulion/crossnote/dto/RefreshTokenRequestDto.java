package com.swulion.crossnote.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 [DTO] 토큰 재발급 요청
 프론트가 Access Token 재발급을 요청할 때,
 Body에 답아 보낼 Refresh Token을 받기 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class RefreshTokenRequestDto {
    @NotBlank(message = "Refresh Token은 필수입니다.")
    private String refreshToken;
}
