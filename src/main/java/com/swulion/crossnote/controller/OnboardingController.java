package com.swulion.crossnote.controller;

import com.swulion.crossnote.dto.Onboarding.*;
import com.swulion.crossnote.service.OnboardingService;
import com.swulion.crossnote.service.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    /*
     구글 기본정보 받아오기 API
     [POST] /onboarding/basic
     */
    @PostMapping("/basic")
    public ResponseEntity<OnboardingResponseDto> updateBasicInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OnboardingGoogleRequestDto dto) {

        String email = userDetails.getUser().getEmail();
        return ResponseEntity.ok(onboardingService.updateGoogleBasicInfo(email, dto));
    }

    /*
     관심정보 API
     [POST] /onboarding/interests
     */
    @PostMapping("/interests")
    public ResponseEntity<OnboardingResponseDto> updateInterests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OnboardingInterestRequestDto dto) {

        String email = userDetails.getUser().getEmail();
        return ResponseEntity.ok(onboardingService.updateUserInterests(email, dto.getInterestNames()));
    }

    /*
     전문분야 API
     [POST] /onboarding/expertise
     */
    @PostMapping("/expertise")
    public ResponseEntity<OnboardingResponseDto> updateExpertise(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OnboardingExpertiseRequestDto dto) {

        String email = userDetails.getUser().getEmail();
        return ResponseEntity.ok(onboardingService.updateUserExpertise(email, dto.getExpertiseNames()));
    }

    /*
     큐레이션 레벨 API
     [POST] /onboarding/curation
     */
    @PostMapping("/curation")
    public ResponseEntity<OnboardingResponseDto> updateCuration(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OnboardingCurationRequestDto dto) {

        String email = userDetails.getUser().getEmail();
        return ResponseEntity.ok(onboardingService.updateCurationLevel(email, dto.getCurationLevel()));
    }

    /*
     유저 정보 조회 API
     [GET] /onboarding/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<OnboardingDetailResponseDto> getUserDetails(@PathVariable Long userId){
        OnboardingDetailResponseDto dto = onboardingService.getUserOnboardingInfo(userId);
        return ResponseEntity.ok(dto);
    }
}
