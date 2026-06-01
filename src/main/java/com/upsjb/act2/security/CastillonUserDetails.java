package com.upsjb.act2.security;

import com.upsjb.act2.util.EstadoUtil;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class CastillonUserDetails implements UserDetails {

    private final Integer idUsuario;
    private final Integer idEmpleado;
    private final Integer idTipoUsuario;
    private final String logeo;
    private final String clave;
    private final String estado;
    private final String nomUsuario;
    private final String nombres;
    private final String apePaterno;
    private final String apeMaterno;
    private final String nomCargo;

    public CastillonUserDetails(
            Integer idUsuario,
            Integer idEmpleado,
            Integer idTipoUsuario,
            String logeo,
            String clave,
            String estado,
            String nomUsuario,
            String nombres,
            String apePaterno,
            String apeMaterno,
            String nomCargo
    ) {
        this.idUsuario = idUsuario;
        this.idEmpleado = idEmpleado;
        this.idTipoUsuario = idTipoUsuario;
        this.logeo = trim(logeo);
        this.clave = trim(clave);
        this.estado = trim(estado);
        this.nomUsuario = trim(nomUsuario);
        this.nombres = trim(nombres);
        this.apePaterno = trim(apePaterno);
        this.apeMaterno = trim(apeMaterno);
        this.nomCargo = trim(nomCargo);
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public Integer getIdTipoUsuario() {
        return idTipoUsuario;
    }

    public String getNomUsuario() {
        return nomUsuario;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApePaterno() {
        return apePaterno;
    }

    public String getApeMaterno() {
        return apeMaterno;
    }

    public String getNomCargo() {
        return nomCargo;
    }

    public String getNombreCompleto() {
        String nombre = String.join(" ",
                nullToEmpty(nombres),
                nullToEmpty(apePaterno),
                nullToEmpty(apeMaterno)
        ).trim();

        return nombre.isBlank() ? logeo : nombre;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(buildRole(nomUsuario)));
    }

    @Override
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return logeo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isEnabled();
    }

    @Override
    public boolean isEnabled() {
        return EstadoUtil.esActivo(estado);
    }

    private static String buildRole(String value) {
        if (value == null || value.isBlank()) {
            return "ROLE_USUARIO";
        }

        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replace(" ", "_")
                .replaceAll("[^A-Z0-9_]", "");

        if (normalized.isBlank()) {
            return "ROLE_USUARIO";
        }

        return "ROLE_" + normalized;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}