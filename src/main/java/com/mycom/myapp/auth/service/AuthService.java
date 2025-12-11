package com.mycom.myapp.auth.service;

import com.mycom.myapp.auth.dto.JoinRequestDto;
import com.mycom.myapp.auth.dto.LoginRequestDto;
import com.mycom.myapp.auth.dto.LoginResponseDto;
import com.mycom.myapp.users.entity.Role;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.entity.UsersRole;
import com.mycom.myapp.users.repository.RoleRepository;
import com.mycom.myapp.users.repository.UsersRepository;
import com.mycom.myapp.users.repository.UsersRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UsersRepository usersRepository;
    private final RoleRepository roleRepository;
    private final UsersRoleRepository usersRoleRepository;

    /**
     * 로그인
     * @param request HttpServletRequest
     * @param dto LoginRequestDto
     * @return LoginResponseDto
     */
    public LoginResponseDto login(HttpServletRequest request, LoginRequestDto dto) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                dto.getEmail(), dto.getPassword()
        );
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        LoginResponseDto responseDto = new LoginResponseDto();
        responseDto.setNickname(authentication.getName());
        return responseDto;
    }

    /**
     * 회원가입
     * @param dto
     * @return 이미지를 업로드하는 경우를 위해 회원 식별자 반환
     */
    public Integer join(JoinRequestDto dto) {
        String encodedPassword = bCryptPasswordEncoder.encode(dto.getPassword());
        Users users = Users.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .nickname(dto.getNickname())
                .description(dto.getDescription())
                .build();
        usersRepository.save(users);

        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(() ->
                new RuntimeException("Role Not Found"));
        usersRoleRepository.save(UsersRole.usersRoleof(users, role));
        return users.getUserId();
    }

    /**
     * 이메일 중복 여부 검증
     * @param email 검증하려는 이메일
     * @return true: 사용 가능한 이메일, false: 이미 사용중인 이메일(사용불가)
     */
    public boolean checkEmailDup(String email) {
        return !usersRepository.existsByEmail(email);
    }
}
