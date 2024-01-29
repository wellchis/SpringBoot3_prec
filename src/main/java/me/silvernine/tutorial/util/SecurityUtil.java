package me.silvernine.tutorial.util;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    public SecurityUtil() {
    }

    // Security Context의 Authentication 객체에서 username을 리턴해주는 메소드
    // (Security Context에 Authentication 객체가 저장되는 시점은 JwtFilter의 doFilter메소드에서
    // Request가 들어올 때 SecurityContext에 Authentication 객체가 저장된다.)
    public static Optional<String> getCurrentUsername() {
        logger.debug("[getCurrentUsername] 시작");
        // Security Context에서 Authentication 정보를 가져온다.
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 정보가 없는 경우
        if(authentication == null){
            logger.debug("Security Context에 인증 정보가 없습니다.");
            return Optional.empty();
        }

        // 있으면 String 객체에 username을 담는다.
        String username = null;
        if(authentication.getPrincipal() instanceof UserDetails){
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            logger.debug("[getCurrentUsername] authentication: {}",authentication);
            username = springSecurityUser.getUsername();
        } else if(authentication.getPrincipal() instanceof String){
            username = (String) authentication.getPrincipal();
        }

        logger.debug("[getCurrentUsername] 끝");
        return Optional.ofNullable(username);
    }
}
