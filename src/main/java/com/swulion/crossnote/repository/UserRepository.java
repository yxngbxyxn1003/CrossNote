package com.swulion.crossnote.repository;

import com.swulion.crossnote.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /* [회원가입 시] */
    // 이메일 중복 검사
    // select count(*) > 0 from user where email = ?
    boolean existsByEmail(String email);

    // 이름 중복 검사
    // select count(*) > 0 from user where name = ?
    boolean existsByName(String name);

    /* [로그인 시] */
    // select * from user where email = ?
    Optional<User> findByEmail(String email);
}
