package me.silvernine.tutorial.config;

import me.silvernine.tutorial.jwt.JwtAccessDeniedHandler;
import me.silvernine.tutorial.jwt.JwtAuthenticationEntryPoint;
import me.silvernine.tutorial.jwt.JwtSecurityConfig;
import me.silvernine.tutorial.jwt.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

@Configuration
@EnableWebSecurity // Web 보안을 활성화 하겠다는 어노테이션
// @EnableGlobalMethodSecurity -> @EnableMethodSecurity
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize를 메소드 단위로 추가하기 위해서 사용
public class SecurityConfig {

    private final static Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // 생성자, 주입받는다.
    public SecurityConfig(TokenProvider tokenProvider,
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
        JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // spring boot 3
    @Bean
    public WebSecurityCustomizer configure() {
        return (web -> web.ignoring()
            .requestMatchers(new AntPathRequestMatcher("/h2-console/**"))
            .requestMatchers(new AntPathRequestMatcher("/favicon.ico")));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            // csrf옵션과 sameOrigin 정책을 허용시켜서 iframe에 대한 접근이 허용되어야 403 에러 안 나오고 접근 및 테스트 가능

            // csrf
            // 사이트 요청 간 위조 보안(disable) => non-browser면 disable 해도 상관없음
            .csrf(AbstractHttpConfigurer::disable)

            // Header // h2-console을 위한 설정
            .headers(headersConfigurer ->
                headersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

            // Authorization
            .authorizeHttpRequests((registry) -> {
                try {
                    // new AntMatchers로도 가능
                    registry.requestMatchers("/api/hello").permitAll()
                        .requestMatchers("/api/authenticate").permitAll()
                        .requestMatchers("/api/signup").permitAll()
                        .anyRequest().authenticated();
                } catch (Exception e){
                    e.printStackTrace();
                }
            })
            // Exception
            .exceptionHandling(
                httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
            )
            // Session // 사용 안 해서 STATELESS
            .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .apply(new JwtSecurityConfig(tokenProvider))
            ;

            // Apply
//            .apply(new JwtSecurityConfig(tokenProvider));
        return httpSecurity.build();
    }

}
