package com.academit.education.security;

import com.academit.education.model.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String rolName = usuario.getRol() != null ? usuario.getRol().getNombre() : "USER";
        if (!rolName.startsWith("ROLE_")) {
            rolName = "ROLE_" + rolName;
        }
        return List.of(new SimpleGrantedAuthority(rolName));
    }

    @Override
    public String getPassword() {
        return usuario.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(usuario.getActivo());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(usuario.getActivo());
    }

    public Integer getIdUsuario() {
        return usuario.getId();
    }

    public String getRolNombre() {
        return usuario.getRol() != null ? usuario.getRol().getNombre() : "";
    }
}

