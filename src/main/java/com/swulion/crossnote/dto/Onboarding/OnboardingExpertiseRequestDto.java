package com.swulion.crossnote.dto.Onboarding;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OnboardingExpertiseRequestDto {
    @NotNull(message = "전문분야는 필수입니다.")
    @Size(min= 3, max = 4, message = "전문 분야는 최소 3개, 최대 4개까지 선택할 수 있습니다.")
    private List<String> expertiseNames;
}
