package com.mycom.myapp.auth.details;

import com.mycom.myapp.users.entity.Users;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Integer id;              // DB PK
    private final String email;         // 로그인 용
    private final String nickname;      // 화면 표시용
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;

    @Builder
    public CustomUserDetails(Users users, Collection<? extends GrantedAuthority> authorities, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, boolean enabled) {
        this.id = users.getUsersId();
        this.email = users.getEmail();
        this.nickname = users.getNickname();
        this.password = users.getPassword();
        this.authorities = authorities;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.enabled = enabled;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        // Spring Security에서 사용하는 username을 이메일로 통일
        return this.email;
    }
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
