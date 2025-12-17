package com.mycom.myapp.auth.service;

import com.mycom.myapp.auth.dto.JoinRequestDto;
import com.mycom.myapp.auth.dto.JoinResponseDto;
import com.mycom.myapp.auth.dto.LoginRequestDto;
import com.mycom.myapp.auth.dto.LoginResponseDto;
import com.mycom.myapp.users.entity.Role;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.RoleRepository;
import com.mycom.myapp.users.repository.UsersRepository;
import com.mycom.myapp.users.repository.UsersRoleRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock UsersRepository usersRepository;
    @Mock RoleRepository roleRepository;
    @Mock UsersRoleRepository usersRoleRepository;
    @InjectMocks AuthServiceImpl authService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_sets_security_context_and_session() {
        // given
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("user@test.com");
        dto.setPassword("pass");

        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("nickname");
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(auth);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        given(request.getSession(true)).willReturn(session);

        // when
        LoginResponseDto resp = authService.login(request, dto);

        // then
        assertThat(resp.getNickname()).isEqualTo("nickname");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(auth);
        verify(session).setAttribute(eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY), any());
    }

    @Test
    void join_creates_user_and_role() throws Exception {
        // given
        JoinRequestDto dto = new JoinRequestDto();
        dto.setEmail("user@test.com");
        dto.setPassword("rawpass");
        dto.setNickname("nick");
        dto.setDescription("desc");

        Role role = Role.of("ROLE_USER");
        setField(role, "roleId", 1);

        given(passwordEncoder.encode("rawpass")).willReturn("encoded");
        given(usersRepository.save(any(Users.class))).willAnswer(invocation -> {
            Users u = invocation.getArgument(0);
            setField(u, "usersId", 10);
            return u;
        });
        given(roleRepository.findByName("ROLE_USER")).willReturn(Optional.of(role));

        // when
        JoinResponseDto resp = authService.join(dto);

        // then
        assertThat(resp.getUsersId()).isEqualTo(10);

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(usersRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(userCaptor.getValue().getEmail()).isEqualTo(dto.getEmail());

        verify(usersRoleRepository).save(any());
    }

    @Test
    void checkEmailDup_throws_when_exists() {
        given(usersRepository.existsByEmail("dup@test.com")).willReturn(true);

        assertThatThrownBy(() -> authService.checkEmailDup("dup@test.com"))
                .isInstanceOf(RuntimeException.class);
        verify(usersRepository, never()).save(any());
    }

    @Test
    void logout_delegates_to_request() throws ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        authService.logout(request);
        verify(request).logout();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
