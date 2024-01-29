package me.silvernine.tutorial.service;

import java.util.Collections;
import java.util.Optional;
import me.silvernine.tutorial.dto.UserDto;
import me.silvernine.tutorial.entity.Authority;
import me.silvernine.tutorial.entity.Users;
import me.silvernine.tutorial.repository.UserRepository;
import me.silvernine.tutorial.util.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입 로직
    @Transactional
    public Users signup(UserDto userDto){
        // 회원 정보가 이미 있는지
        if(userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null){
            throw new RuntimeException("이미 가입되어 있는 유저입니다.");
        }

        // 권한 정보 생성
        Authority authority = Authority.builder()
            .authorityName("ROLE_USER")
            .build();

        // 유저 정보 생성
        Users user = Users.builder()
            .username(userDto.getUsername())
            .password(passwordEncoder.encode(userDto.getPassword()))
            .nickname(userDto.getNickname())
            .authorities(Collections.singleton(authority))
            .activated(true)
            .build();

        // 유저+권한정보 DB에 저장
        return userRepository.save(user);
    }

    // username을 기준으로 정보를 가져오는 메소드
    @Transactional(readOnly = true)
    public Optional<Users> getUserWithAuthorities(String username){
        return userRepository.findOneWithAuthoritiesByUsername(username);
    }

    // SecurityContext에 저장된 username 정보만 가져오는 메소드
    @Transactional(readOnly = true)
    public Optional<Users> getMyUserWithAuthorities(){
        return SecurityUtil.getCurrentUsername().flatMap(userRepository::findOneWithAuthoritiesByUsername);
    }
}
