package com.swulion.crossnote.service;

import com.swulion.crossnote.dto.Onboarding.OnboardingDetailResponseDto;
import com.swulion.crossnote.dto.Onboarding.OnboardingGoogleRequestDto;
import com.swulion.crossnote.dto.Onboarding.OnboardingResponseDto;
import com.swulion.crossnote.entity.*;
import com.swulion.crossnote.repository.CategoryRepository;
import com.swulion.crossnote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // 구글 전용 정보 받기
    public OnboardingResponseDto updateGoogleBasicInfo(String email, OnboardingGoogleRequestDto requestDto) {
        User user = findUserByEmail(email);
        user.setGender(requestDto.getGender());
        user.setBirthDate(requestDto.getBirthdate());
        return new OnboardingResponseDto(user.getUserId());
    }

    // 관심분야 업데이트
    public OnboardingResponseDto updateUserInterests(String email, List<Long> interestIds) {
        User user = findUserByEmail(email);
        savePreferences(user, interestIds, PreferenceType.INTEREST);
        return new OnboardingResponseDto(user.getUserId());
    }

    // 전문분야 업데이트
    public OnboardingResponseDto updateUserExpertise(String email, List<Long> expertiseIds) {
        User user = findUserByEmail(email);
        savePreferences(user, expertiseIds, PreferenceType.EXPERTISE);
        return new OnboardingResponseDto(user.getUserId());
    }

    // 큐레이션 단계 업데이트
    public OnboardingResponseDto updateCurationLevel(String email, CurationLevel curationLevel) {
        User user = findUserByEmail(email);
        user.setCurationLevel(curationLevel);
        return new OnboardingResponseDto(user.getUserId());
    }

    /*
     관심분야/전문분야 저장
     기존 동일 타입 엔티티 제거 후 새로 매핑
     */
    private void savePreferences(User user, List<Long> categoryIds, PreferenceType type) {
        // 기존 동일 타입 제거
        // removeIf를 사용하면 내부 Set을 안전하게 순회하면서 삭제 가능
        user.getPreferences().removeIf(p -> p.getPreferenceType() == type);

        // 새 관심/전문 매핑
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 ID: " + categoryId));

            // 전문분야인 경우 상위 카테고리 선택 방지
            if (type == PreferenceType.EXPERTISE && category.getParentCategoryId() == null) {
                throw new IllegalArgumentException("상위 카테고리는 전문분야로 선택할 수 없습니다.");
            }

            UserCategoryPreference preference = UserCategoryPreference.create(user, category, type);
            user.getPreferences().add(preference);
        }

        userRepository.save(user);
    }

    // 조회 메서드
    @Transactional
    public OnboardingDetailResponseDto getUserOnboardingInfo(Long userId) {
        User user = findUserById(userId);

        List<Long> interestIds = new ArrayList<>();
        List<Long> expertiseIds = new ArrayList<>();

        for (UserCategoryPreference pref : user.getPreferences()) {
            if (pref.getPreferenceType() == PreferenceType.INTEREST) {
                interestIds.add(pref.getCategory().getCategoryId());
            } else if (pref.getPreferenceType() == PreferenceType.EXPERTISE) {
                expertiseIds.add(pref.getCategory().getCategoryId());
            }
        }

        return new OnboardingDetailResponseDto(
                user.getUserId(),
                user.getEmail(),
                interestIds,
                expertiseIds,
                user.getCurationLevel()
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("온보딩 중 유저를 찾을 수 없습니다: " + email));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("온보딩 중 유저를 찾을 수 없습니다: " + userId));
    }
}
