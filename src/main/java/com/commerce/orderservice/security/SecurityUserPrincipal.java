package com.commerce.orderservice.security;

import com.commerce.orderservice.entity.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUserPrincipal implements UserDetails {

    private final User user;

    public SecurityUserPrincipal(User user) {
        this.user = user;
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getRoleName() {
        return user.getRole().name();
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
