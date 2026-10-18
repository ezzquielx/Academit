package com.academit.education.security;

import com.academit.education.model.Usuario;
import com.academit.education.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .or(() -> {
                    // Soporte para alias de prueba: docente -> docente1, alumno -> alumno1
                    if ("docente".equalsIgnoreCase(username)) {
                        return usuarioRepository.findByUsername("docente1");
                    } else if ("alumno".equalsIgnoreCase(username)) {
                        return usuarioRepository.findByUsername("alumno1");
                    }
                    return java.util.Optional.empty();
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con username: " + username));

        return new CustomUserDetails(usuario);
    }
}

