package com.exerciting.Exerciting.Infrastructure.security;

import com.exerciting.Exerciting.Domain.user.entity.Role;
import com.exerciting.Exerciting.Domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class LoginUser implements UserDetails {

    private final Long id;
    private final String loginId;
    private final Role role;

    public LoginUser(Long id, String loginId) {
        this(id, loginId, Role.USER);
    }

    public LoginUser(Long id, String loginId, Role role) {
        this.id = id;
        this.loginId = loginId;
        this.role = role == null ? Role.USER : role;
    }

    public static LoginUser from(User user) {
        return new LoginUser(user.getId(), user.getUserId(), user.getRole());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
