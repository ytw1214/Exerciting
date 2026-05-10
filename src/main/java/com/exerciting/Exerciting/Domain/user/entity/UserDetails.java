package com.exerciting.Exerciting.Domain.user.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {


    private final Long id;
    private final String userId;
    private final String password;

    public UserDetails(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.password = user.getPw();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    public String getUsername() {
        return userId;
    }
    public String getPassword() {
        return password;
    }
    public boolean isAccountNonExpired() {
        return true;
    }
    public boolean isAccountNonLocked() {
        return true;
    }
    public boolean isCredentialsNonExpired() {
        return true;
    }
    public boolean isEnabled() {
        return true;
    }
}
