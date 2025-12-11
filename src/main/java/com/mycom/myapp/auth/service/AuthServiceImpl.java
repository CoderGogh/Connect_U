package com.mycom.myapp.auth.service;

import com.mycom.myapp.auth.dto.JoinRequestDto;
import com.mycom.myapp.auth.dto.JoinResponseDto;
import com.mycom.myapp.auth.dto.LoginRequestDto;
import com.mycom.myapp.auth.dto.LoginResponseDto;
import com.mycom.myapp.users.entity.Role;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.entity.UsersRole;
import com.mycom.myapp.users.repository.RoleRepository;
import com.mycom.myapp.users.repository.UsersRepository;
import com.mycom.myapp.users.repository.UsersRoleRepository;
import jakarta.servlet.ServletException;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
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
    @Override
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
     * @param dto JoinRequestDto
     * @return JoinResponseDto
     */
    @Override
    @Transactional
    public JoinResponseDto join(JoinRequestDto dto) {
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
        JoinResponseDto responseDto = new JoinResponseDto();
        responseDto.setUsersId(users.getUsersId());
        return responseDto;
    }

    /**
     * 이메일 중복 여부 검증
     * @param email 검증하려는 이메일
     * @throws RuntimeException 이메일 중복 시 예외 발생
     */
    @Override
    public void checkEmailDup(String email) {
        if(usersRepository.existsByEmail(email)) {
            throw new RuntimeException("Email Already Exists");
        }
    }

    /**
     * 로그아웃(스프링 시큐리티 표준 로그아웃 로직 호출)
     * @param request HttpServletRequest
     * @throws ServletException
     */
    @Override
    public void logout(HttpServletRequest request) throws ServletException {
        request.logout();
    }
}
