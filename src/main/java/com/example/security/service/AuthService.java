package com.example.security.service;

import com.example.security.entity.User;
import com.example.security.repository.UserRepository;
import com.example.security.dto.LoginRequest;
import com.example.security.dto.LoginResponse;
import com.example.security.dto.SignupRequest;
import com.example.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public String signup(SignupRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 2. 비밀번호 암호화 및 유저 객체 생성
        User user = User.builder()
                .id(request.getId())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 암호화 핵심!!!!
                .username(request.getUsername())
                .role("ROLE_USER")
                .build();

        // 3. DB 저장
        userRepository.save(user);

        return "회원가입 성공!";
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request){

        // 1. ID로 유저 찾기
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 2. 비밀번호 일치 확인 (암호화된 비번 vs 입력된 비번)
        // 순서 주의: matches (인입 비번, 암호화된 비번)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 진짜 토큰 생성
        String token = jwtUtil.createToken(user.getId());

        // 4. 생성한 토큰을 응답 객체에 담아서 마지막에 리턴!
        System.out.println("token: " + token);

        return LoginResponse.builder()
                .message("로그인 성공!")
                .token(token)
                .build();
    }

}
