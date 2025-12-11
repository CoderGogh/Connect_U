package com.mycom.myapp.auth.service;

import com.mycom.myapp.auth.details.CustomUserDetails;
import com.mycom.myapp.users.entity.Users;
import com.mycom.myapp.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UsersRepository usersRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users users = usersRepository.findByEmailForLogin(email).orElseThrow(() ->
                new UsernameNotFoundException(email));
        Collection<? extends GrantedAuthority> authorities = users.getUsersRoles().stream()
                .map(usersRole -> new SimpleGrantedAuthority(usersRole.getRole().getName()))
                .toList();

        // CustomUserDetails 생성 후 Return
        return CustomUserDetails.builder()
                .users(users)
                .authorities(authorities)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
    }
}
