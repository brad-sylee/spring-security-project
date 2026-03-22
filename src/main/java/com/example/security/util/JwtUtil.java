package com.example.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // 토큰 만료 시간: 1시간 (1000ms * 60초 * 60분)
    private final long EXPIRATION_TIME = 1000L * 60 * 60;

    // 1. 키 생성 보조 메서드 (내부에서만 사용)
    public SecretKey getSigninKey(){
        System.out.println("주입된 키 확인: " + jwtSecret); // 👈 로그 찍어보기
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 2. 토큰 생성
    public String createToken(String userId){
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(userId)            // userId를 'sub' 클레임에 저장
                .issuedAt(now)              // 토큰 발행 시간
                .expiration(expiration)     // 토큰 만료 시간
                .signWith(getSigninKey())   // 비밀키로 서명
                .compact();
    }

    // 3. 토큰에서 UserId 뽑기
    public String getUserId(String token){
        Claims claims = Jwts.parser()
                .verifyWith(getSigninKey())     // 서명 검증
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();  // 저장했던 userId 반환
    }

    // 4. 토큰 유형성 검증 (만료됐는지, 변조됐는지)
    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(getSigninKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch(Exception e){

            // 토큰이 만료, 변조 된 경우 에러가 발생하여 false 반환
            return false;
        }
    }

}
