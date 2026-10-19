package dev.shiori.infra.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import dev.shiori.domain.enums.Role;
import dev.shiori.domain.model.Usuario;

public class UsuarioPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String senha;
    private final Role role;
    private final boolean ativo;
    private final boolean onboardingConcluido;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioPrincipal(
            Long id,
            String email,
            String senha,
            Role role,
            boolean ativo,
            boolean onboardingConcluido) {
        this.id = id;
        this.email = email;
        this.senha = senha;
        this.role = role != null ? role : Role.ROLE_USER;
        this.ativo = ativo;
        this.onboardingConcluido = onboardingConcluido;
        this.authorities = List.of(new SimpleGrantedAuthority(this.role.name()));
    }

    public static UsuarioPrincipal aPartirDe(Usuario usuario) {
        return new UsuarioPrincipal(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getSenha(),
                usuario.getRole(),
                usuario.isAtivo(),
                usuario.isOnboardingConcluido());
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public boolean isOnboardingConcluido() {
        return onboardingConcluido;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return ativo;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
