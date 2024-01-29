package me.silvernine.tutorial.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import me.silvernine.tutorial.entity.Users;
import me.silvernine.tutorial.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component("userDetailService")
public class CustomUserDetailService implements UserDetailsService {

    private final static Logger logger = LoggerFactory.getLogger(CustomUserDetailService.class);

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    // User 정보를 권한 정보랑 함께 가져오는 메소드
    public UserDetails loadUserByUsername(final String username) {
        logger.debug("[loadUserByUsername] 실행");
        logger.debug("[loadUserByUsername] userRepository.findOneWithAuthoritiesByUsername(username) ==> {}",userRepository.findOneWithAuthoritiesByUsername(username));
        logger.debug("[loadUserByUsername] 종료");
        return userRepository.findOneWithAuthoritiesByUsername(username)
            .map(user -> createUser(username, user))
            .orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));
    }

    private User createUser(String username, Users user) {

        // 활성화 상태인 경우
        if (!user.isActivated()) {
            throw new RuntimeException(username + " -> 활성화되어 있지 않습니다.");
        }

        user.test();

        // 권한정보와 유저정보 반환
        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
            .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName()))
            .collect(Collectors.toList());
        logger.debug("[createUser] grantedAuthorities {} :", grantedAuthorities);
        return new User(user.getUsername(), user.getPassword(), grantedAuthorities);
    }
}
