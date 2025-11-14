package com.swulion.crossnote.dto.Onboarding;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OnboardingInterestRequestDto {
    @NotNull(message = "관심분야는 필수입니다.")
    private List<String> interestNames;
}
