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
    public OnboardingResponseDto updateUserInterests(String email, List<String> interestNames) {
        User user = findUserByEmail(email);
        savePreferences(user, interestNames, PreferenceType.INTEREST);
        return new OnboardingResponseDto(user.getUserId());
    }

    // 전문분야 업데이트
    public OnboardingResponseDto updateUserExpertise(String email, List<String> expertiseNames) {
        User user = findUserByEmail(email);
        savePreferences(user, expertiseNames, PreferenceType.EXPERTISE);
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
    private void savePreferences(User user, List<String> categoryNames, PreferenceType type) {
        // 삭제
        user.getPreferences().removeIf(p -> p.getPreferenceType() == type);
        userRepository.save(user); // 삭제 내역 반영
        userRepository.flush();    // 강제 반영 (필요시 clear도 고려)

        // 추가
        for (String name : categoryNames) {
            Category category = categoryRepository.findByCategoryName(name);
            if (category == null) throw new IllegalArgumentException("존재하지 않는 카테고리 이름: " + name);
            if (type == PreferenceType.EXPERTISE && category.getParentCategoryId() == null)
                throw new IllegalArgumentException("상위 카테고리는 전문분야로 선택할 수 없습니다.");
            UserCategoryPreference preference = UserCategoryPreference.create(user, category, type);
            user.getPreferences().add(preference);
        }
        userRepository.save(user);
    }

    // 조회 메서드
    @Transactional
    public OnboardingDetailResponseDto getUserOnboardingInfo(Long userId) {
        User user = findUserById(userId);

        List<String> interestNames = new ArrayList<>();
        List<String> expertiseNames = new ArrayList<>();

        for (UserCategoryPreference pref : user.getPreferences()) {
            if (pref.getPreferenceType() == PreferenceType.INTEREST) {
                interestNames.add(pref.getCategory().getCategoryName());
            } else if (pref.getPreferenceType() == PreferenceType.EXPERTISE) {
                expertiseNames.add(pref.getCategory().getCategoryName());
            }
        }

        return new OnboardingDetailResponseDto(
                user.getUserId(),
                user.getEmail(),
                interestNames,
                expertiseNames,
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
