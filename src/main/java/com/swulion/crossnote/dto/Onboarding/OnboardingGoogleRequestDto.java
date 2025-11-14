package com.swulion.crossnote.dto.Onboarding;

import com.swulion.crossnote.entity.Gender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class OnboardingGoogleRequestDto {
    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;

    @NotNull(message = "생년월일은 필수입니다.")
    @PastOrPresent(message = "생년월일은 현재 또는 과거 날짜여야 합니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) //YYYY-MM-DD 형식
    private LocalDate birthdate;
}
