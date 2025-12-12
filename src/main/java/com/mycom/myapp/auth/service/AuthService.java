package com.mycom.myapp.auth.service;

import com.mycom.myapp.auth.dto.JoinRequestDto;
import com.mycom.myapp.auth.dto.JoinResponseDto;
import com.mycom.myapp.auth.dto.LoginRequestDto;
import com.mycom.myapp.auth.dto.LoginResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    LoginResponseDto login(HttpServletRequest request, LoginRequestDto dto);
    JoinResponseDto join(JoinRequestDto dto);
    void checkEmailDup(String email);
    void logout(HttpServletRequest request) throws ServletException;
}
