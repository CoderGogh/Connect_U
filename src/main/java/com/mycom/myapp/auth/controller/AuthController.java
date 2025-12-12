package com.mycom.myapp.auth.controller;

import com.mycom.myapp.auth.dto.JoinRequestDto;
import com.mycom.myapp.auth.dto.JoinResponseDto;
import com.mycom.myapp.auth.dto.LoginRequestDto;
import com.mycom.myapp.auth.dto.LoginResponseDto;
import com.mycom.myapp.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth API", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ResponseEntity<LoginResponseDto> login(HttpServletRequest request, @RequestBody @Valid LoginRequestDto dto) {
        return ResponseEntity.ok().body(authService.login(request,dto));
    }

    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "이미지는 회원가입 후 별도의 API를 통해 업로드합니다.")
    public ResponseEntity<JoinResponseDto> join(@RequestBody @Valid JoinRequestDto dto) {
        return ResponseEntity.ok().body(authService.join(dto));
    }

    @GetMapping("/check-email/{email}")
    @Operation(summary = "이메일 중복 확인")
    public ResponseEntity<Void> checkEmail(@PathVariable String email) {
        authService.checkEmailDup(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "해당 유저의 세션을 만료시킵니다.")
    public ResponseEntity<Void> logout(HttpServletRequest request) throws ServletException {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/csrf") // 백엔드 개발 시 csrf 토큰 발급을 위한 임시 api 입니다.
    @Operation(summary = "백엔드 개발 API 테스트를 위한 CSRF 토큰 발급 API입니다.", description = "Postman 테스트 시 'X-CSRF-TOKEN' 헤더에 토큰 값을 첨부해주세요.")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }
}
