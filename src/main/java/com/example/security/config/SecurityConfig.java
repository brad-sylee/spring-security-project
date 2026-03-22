package com.example.security.config;

import com.example.security.jwt.JwtFilter;
import com.example.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // final 필드를 위한 생성자를 자동으로 만들어 주는 어노테이션
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public String debugBean() {
        System.out.println(">>> [DEBUG] SecurityConfig가 로드되었습니다! <<<");
        return "debug";
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CSRF 끔. JWT 사용하는 Rest API, MSA구조에서는 쿠키를 이용한 공격 위험이 적고, 매번 토큰을 검증하기 때문.
                .csrf(csrf -> csrf.disable())

                // formLogin 및 HTTP Basic 인증 비활성화
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasicAuth -> httpBasicAuth.disable())

                // sessionManagement에 try-catch 걸지 않는 이유는 람다식으로 "세션 관리 설정은 이렇게 해줘"라고 규칙을 전달했기 때문. Spring Security 6.1부터 builder 패턴이 아닌, 아래와 같은 함수형 설정을 강력히 권고하고 있다.
                // 사용자 상태를 저장하지 않음. STATELESS = 무상태. 토큰으로 인증하기 때문에 저장하지 않음.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // **통행권** 설정하는 곳.
                .authorizeHttpRequests(auth -> auth
                        // "/api/auth/**"로 시작하는 모든 요청(회원가입, 로그인)은 누구에게나(permit All) 허용하겠다.
                        .requestMatchers("/api/auth/**").permitAll()
                        // 그 외에 요청은 반드시 **인증(Authenticated)**된 사람만 들어올 수 있음.
                        .anyRequest().authenticated()
                )

                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        System.out.println(">>> [DEBUG] FilterChain 설정이 완료되었습니다! <<<");
        return http.build();
    }

    // 비밀번호 암호화 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt는 강령한 해시 알고리즘.
    }

}
