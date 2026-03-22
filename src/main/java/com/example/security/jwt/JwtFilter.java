package com.example.security.jwt;

import com.example.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override  // 내가 직접 코드에서 사용하지 않지만 Tomcat, Spring에서 직접 사용하기 때문에 필요함.
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // 1. 요청 헤더에서 Authorization 값을 꺼냄
        String authorization = request.getHeader("Authorization");

        // 2. 토큰이 있고 "Bearer "로 시작하는지 확인
        if(authorization != null && authorization.startsWith("Bearer ")){
            String token = authorization.substring(7); // "Bearer " 뒷부분(토큰)만 추출

            // 3. 토큰이 유효한지 검증 (JwtUtil에 만든 메서드)
            if(jwtUtil.validateToken(token)){
                String userId = jwtUtil.getUserId(token);

                // 4. Spring Security 전용 '인증 도장' 찍기
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

                // 5. Security session(바구니)에 이 도장을 담아둠 (이후 Controller에서 꺼내 쓸 수 있음)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        }

        // 6. 다음 Filter(검문소)로 통과 시켜줌
        filterChain.doFilter(request, response);
    }

}
