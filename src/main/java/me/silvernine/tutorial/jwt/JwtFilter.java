package me.silvernine.tutorial.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private TokenProvider tokenProvider;

    // tokenProvider를 주입받음
    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    // Jwt Token의 인증정보를 현재 실행중인 SecurityContext에 저장하기 위한 메소드
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        logger.debug("[doFilter] 시작");
        // servletRequest 까기
        System.out.println("==========================시작=========================");
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        Enumeration enumReqNames = request.getParameterNames();
        String name = null;
        String [] values = null;
        int cntValue = 0;

        while(enumReqNames.hasMoreElements()){
            name = (String) enumReqNames.nextElement();
            values = request.getParameterValues(name);
            cntValue = values.length;

            for(int i=0; i<cntValue; i++){
                System.out.println("functionName : " + name + "/ [" + values[i] + "]");
            }
        }
        System.out.println("==========================종료=========================");


        logger.debug("[doFilter] servletRequest : {}", servletRequest);
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        logger.debug("[doFilter] httpServletRequest ==> {}", httpServletRequest);
        // Request에서 token을 받아서
        String jwt = resolveToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();

        // 유효성 검증 통과하면
        if (StringUtils.hasText(jwt) && tokenProvider.validationToken(jwt)){
            logger.debug("[doFilter] 유효성 검증 통과");
            // Authentication 객체를 받아와서
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            // SecurityContext에 set(인증)
            logger.debug("[doFilter] authentication ==> {}", authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);

            logger.debug("'{}'의 정보 내용은 '{}'입니다. {}",authentication.getName(), authentication.getAuthorities(), authentication.getPrincipal());
        } else {
            logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        filterChain.doFilter(servletRequest, servletResponse);

        logger.debug("[doFilter] 종료");
    }

    // Token 정보를 꺼내오기 위한 메소드
    private String resolveToken(HttpServletRequest request) {
        // request Header에서 회원정보 꺼내오는
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
