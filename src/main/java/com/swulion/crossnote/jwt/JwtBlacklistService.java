package com.swulion.crossnote.jwt;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 JWT 블랙리스트를 관리하는 서비스
 (간단한 구현을 위해 메모리 기반 Set 사용)
 */
@Service
public class JwtBlacklistService {

    // TODO:
    // 메모리 기반 블랙리스트는 서버가 재시작되면 초기화됨. 즉,
    // 배포 환경에서는 Redis 사용하여
    // 토큰의 만료 시간(exp)만큼만 블랙리스트에 저장하는 방식(Redis의 TTL) 고려
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    /*
     토큰을 블랙리스트에 추가
     (로그아웃할 JWT)
     */
    public void blacklistToken(String token) {
        blacklist.add(token);
    }

    /*
     토큰이 블랙리스트에 있는지 확인
     (검사할 JWT)
     @return 블랙리스트에 있으면 true
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklist.contains(token);
    }
}