package com.example.security.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {
    private String id;
    private String email;
    private String password;
    private String username;
    // role은 default로 'role_user'로 줄 예정이라 생략 함.
}
