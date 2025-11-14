package com.swulion.crossnote.dto.Onboarding;

import com.swulion.crossnote.entity.CurationLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class OnboardingCurationRequestDto {
    @NotNull(message = "큐레이션 레벨은 필수입니다.")
    private CurationLevel curationLevel;
}