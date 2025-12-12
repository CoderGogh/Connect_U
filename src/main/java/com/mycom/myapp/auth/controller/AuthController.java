package com.mycom.myapp.auth.controller;

import com.mycom.myapp.auth.dto.JoinRequestDto;
import com.mycom.myapp.auth.dto.JoinResponseDto;
import com.mycom.myapp.auth.dto.LoginRequestDto;
import com.mycom.myapp.auth.dto.LoginResponseDto;
import com.mycom.myapp.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(HttpServletRequest request, @RequestBody @Validated LoginRequestDto dto) {
        return ResponseEntity.ok().body(authService.login(request,dto));
    }

    @PostMapping("/join")
    public ResponseEntity<JoinResponseDto> join(@RequestBody @Validated JoinRequestDto dto) {
        return ResponseEntity.ok().body(authService.join(dto));
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<Void> checkEmail(@PathVariable String email) {
        authService.checkEmailDup(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) throws ServletException {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/csrf") // 백엔드 개발 시 csrf 토큰 발급을 위한 임시 api 입니다.
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }
}
