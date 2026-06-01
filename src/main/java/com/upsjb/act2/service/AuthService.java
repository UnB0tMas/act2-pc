package com.upsjb.act2.service;

import com.upsjb.act2.security.CastillonUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    public Optional<CastillonUserDetails> obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return Optional.empty();
        }

        if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CastillonUserDetails userDetails) {
            return Optional.of(userDetails);
        }

        return Optional.empty();
    }

    public String obtenerNombreVisible() {
        return obtenerUsuarioAutenticado()
                .map(CastillonUserDetails::getNombreCompleto)
                .orElse("Usuario");
    }

    public String obtenerTipoUsuario() {
        return obtenerUsuarioAutenticado()
                .map(CastillonUserDetails::getNomUsuario)
                .orElse("USUARIO");
    }

    public Integer obtenerIdUsuario() {
        return obtenerUsuarioAutenticado()
                .map(CastillonUserDetails::getIdUsuario)
                .orElse(null);
    }

    public Integer obtenerIdEmpleado() {
        return obtenerUsuarioAutenticado()
                .map(CastillonUserDetails::getIdEmpleado)
                .orElse(null);
    }
}