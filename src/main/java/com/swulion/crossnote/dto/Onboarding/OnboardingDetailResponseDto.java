package com.swulion.crossnote.dto.Onboarding;

import com.swulion.crossnote.entity.CurationLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OnboardingDetailResponseDto {
    private Long userId;

    private String email;
    private List<String> interestNames;
    private List<String> expertiseNames;
    private CurationLevel curationLevel;
}
