package com.upsjb.act2.repository;

import com.upsjb.act2.model.Usuario;
import com.upsjb.act2.security.CastillonUserDetails;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

@Repository
public class UsuarioRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public UsuarioRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<CastillonUserDetails> findUserDetailsByLogeo(String logeo) {
        String sql = """
                SELECT
                    u.IDUSUARIO,
                    u.IDEMPLEADO,
                    u.IDTIPO_USUARIO,
                    u.LOGEO,
                    u.CLAVE,
                    u.ESTADO,
                    tu.NOMUSUARIO,
                    p.NOMBRES,
                    p.APEPATERNO,
                    p.APEMATERNO,
                    c.NOMCARGO
                FROM USUARIO u
                LEFT JOIN TIPO_USUARIO tu ON tu.IDTIPO_USUARIO = u.IDTIPO_USUARIO
                LEFT JOIN EMPLEADO e ON e.IDEMPLEADO = u.IDEMPLEADO
                LEFT JOIN PERSONA p ON p.IDPERSONA = e.IDPERSONA
                LEFT JOIN CARGO c ON c.IDCARGO = e.IDCARGO
                WHERE REPLACE(LOWER(LTRIM(RTRIM(u.LOGEO))), ',', '.')
                      COLLATE Latin1_General_CI_AI = :logeoNormalizado COLLATE Latin1_General_CI_AI
                  AND LTRIM(RTRIM(CAST(u.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                  AND (
                        tu.IDTIPO_USUARIO IS NULL
                        OR LTRIM(RTRIM(CAST(tu.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                  )
                  AND (
                        e.IDEMPLEADO IS NULL
                        OR LTRIM(RTRIM(CAST(e.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                  )
                """;

        CastillonUserDetails user = DataAccessUtils.singleResult(
                jdbc.query(
                        sql,
                        new MapSqlParameterSource("logeoNormalizado", normalizarLogeo(logeo)),
                        userDetailsMapper()
                )
        );

        return Optional.ofNullable(user);
    }

    public Optional<Usuario> findById(Integer idUsuario) {
        String sql = """
                SELECT
                    u.IDUSUARIO,
                    u.IDEMPLEADO,
                    u.IDTIPO_USUARIO,
                    u.LOGEO,
                    u.CLAVE,
                    u.ESTADO,
                    tu.NOMUSUARIO,
                    p.NOMBRES,
                    p.APEPATERNO,
                    p.APEMATERNO,
                    c.NOMCARGO
                FROM USUARIO u
                LEFT JOIN TIPO_USUARIO tu ON tu.IDTIPO_USUARIO = u.IDTIPO_USUARIO
                LEFT JOIN EMPLEADO e ON e.IDEMPLEADO = u.IDEMPLEADO
                LEFT JOIN PERSONA p ON p.IDPERSONA = e.IDPERSONA
                LEFT JOIN CARGO c ON c.IDCARGO = e.IDCARGO
                WHERE u.IDUSUARIO = :idUsuario
                """;

        Usuario usuario = DataAccessUtils.singleResult(
                jdbc.query(sql, new MapSqlParameterSource("idUsuario", idUsuario), usuarioMapper())
        );

        return Optional.ofNullable(usuario);
    }

    private static String normalizarLogeo(String logeo) {
        if (logeo == null) {
            return "";
        }

        return logeo.trim()
                .replace(',', '.')
                .toLowerCase(Locale.ROOT);
    }

    private RowMapper<CastillonUserDetails> userDetailsMapper() {
        return (rs, rowNum) -> new CastillonUserDetails(
                rs.getObject("IDUSUARIO", Integer.class),
                rs.getObject("IDEMPLEADO", Integer.class),
                rs.getObject("IDTIPO_USUARIO", Integer.class),
                text(rs, "LOGEO"),
                text(rs, "CLAVE"),
                text(rs, "ESTADO"),
                text(rs, "NOMUSUARIO"),
                text(rs, "NOMBRES"),
                text(rs, "APEPATERNO"),
                text(rs, "APEMATERNO"),
                text(rs, "NOMCARGO")
        );
    }

    private RowMapper<Usuario> usuarioMapper() {
        return (rs, rowNum) -> new Usuario(
                rs.getObject("IDUSUARIO", Integer.class),
                rs.getObject("IDEMPLEADO", Integer.class),
                rs.getObject("IDTIPO_USUARIO", Integer.class),
                text(rs, "LOGEO"),
                text(rs, "CLAVE"),
                text(rs, "ESTADO"),
                text(rs, "NOMUSUARIO"),
                text(rs, "NOMBRES"),
                text(rs, "APEPATERNO"),
                text(rs, "APEMATERNO"),
                text(rs, "NOMCARGO")
        );
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}