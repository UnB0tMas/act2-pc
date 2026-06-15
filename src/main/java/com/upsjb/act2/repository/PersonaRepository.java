package com.upsjb.act2.repository;

import com.upsjb.act2.model.Persona;
import com.upsjb.act2.util.EstadoUtil;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public class PersonaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PersonaRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    public Optional<Persona> findById(Integer idPersona) {
        String sql = """
                SELECT
                    p.IDPERSONA,
                    p.IDDISTRITO,
                    p.NOMBRES,
                    p.APEPATERNO,
                    p.APEMATERNO,
                    p.EST_CIVIL,
                    p.DNI,
                    p.DIRECCION,
                    p.CELULAR,
                    p.FECNAC,
                    p.CORREO,
                    p.ESTADO,
                    d.NOMDISTRITO,
                    pr.NOMPROVINCIA,
                    dep.NOMDEPARTAMENTO
                FROM PERSONA p
                LEFT JOIN DISTRITO d
                    ON d.IDDISTRITO = p.IDDISTRITO
                LEFT JOIN PROVINCIA pr
                    ON pr.IDPROVINCIA = d.IDPROVINCIA
                LEFT JOIN DEPARTAMENTO dep
                    ON dep.IDDEPARTAMENTO = pr.IDDEPARTAMENTO
                WHERE p.IDPERSONA = :idPersona
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idPersona",
                                idPersona
                        );

        Persona persona =
                DataAccessUtils.singleResult(
                        jdbc.query(
                                sql,
                                params,
                                personaMapper()
                        )
                );

        return Optional.ofNullable(persona);
    }

    public boolean existsByDni(String dni) {
        String sql = """
                SELECT COUNT(*)
                FROM PERSONA
                WHERE LTRIM(RTRIM(DNI)) = :dni
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("dni", dni);

        Long total = jdbc.queryForObject(
                sql,
                params,
                Long.class
        );

        return total != null && total > 0;
    }

    public Integer insert(Persona persona) {
        String sql = """
                INSERT INTO PERSONA (
                    IDDISTRITO,
                    NOMBRES,
                    APEPATERNO,
                    APEMATERNO,
                    EST_CIVIL,
                    DNI,
                    DIRECCION,
                    CELULAR,
                    FECNAC,
                    CORREO,
                    ESTADO
                )
                VALUES (
                    :idDistrito,
                    :nombres,
                    :apePaterno,
                    :apeMaterno,
                    :estCivil,
                    :dni,
                    :direccion,
                    :celular,
                    :fecNac,
                    :correo,
                    :estado
                )
                """;

        KeyHolder keyHolder =
                new GeneratedKeyHolder();

        int filasAfectadas = jdbc.update(
                sql,
                params(persona),
                keyHolder,
                new String[]{"IDPERSONA"}
        );

        if (filasAfectadas != 1) {
            throw new IllegalStateException(
                    "No se pudo insertar la persona. "
                            + "Filas afectadas: "
                            + filasAfectadas
            );
        }

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "SQL Server no devolvió el IDPERSONA generado."
            );
        }

        return key.intValue();
    }

    public int update(Persona persona) {
        String sql = """
                UPDATE PERSONA
                SET
                    IDDISTRITO = :idDistrito,
                    NOMBRES = :nombres,
                    APEPATERNO = :apePaterno,
                    APEMATERNO = :apeMaterno,
                    EST_CIVIL = :estCivil,
                    DNI = :dni,
                    DIRECCION = :direccion,
                    CELULAR = :celular,
                    FECNAC = :fecNac,
                    CORREO = :correo,
                    ESTADO = :estado
                WHERE IDPERSONA = :idPersona
                """;

        MapSqlParameterSource params =
                params(persona)
                        .addValue(
                                "idPersona",
                                persona.getIdPersona()
                        );

        return jdbc.update(sql, params);
    }

    private MapSqlParameterSource params(
            Persona persona
    ) {
        return new MapSqlParameterSource()
                .addValue(
                        "idDistrito",
                        persona.getIdDistrito()
                )
                .addValue(
                        "nombres",
                        persona.getNombres()
                )
                .addValue(
                        "apePaterno",
                        persona.getApePaterno()
                )
                .addValue(
                        "apeMaterno",
                        persona.getApeMaterno()
                )
                .addValue(
                        "estCivil",
                        persona.getEstCivil()
                )
                .addValue(
                        "dni",
                        persona.getDni()
                )
                .addValue(
                        "direccion",
                        persona.getDireccion()
                )
                .addValue(
                        "celular",
                        persona.getCelular()
                )
                .addValue(
                        "fecNac",
                        persona.getFecNac()
                )
                .addValue(
                        "correo",
                        persona.getCorreo()
                )
                .addValue(
                        "estado",
                        EstadoUtil.normalizar(
                                persona.getEstado()
                        )
                );
    }

    private RowMapper<Persona> personaMapper() {
        return (rs, rowNum) -> {
            String nombres =
                    text(rs, "NOMBRES");

            String apePaterno =
                    text(rs, "APEPATERNO");

            String apeMaterno =
                    text(rs, "APEMATERNO");

            return new Persona(
                    rs.getObject(
                            "IDPERSONA",
                            Integer.class
                    ),
                    rs.getObject(
                            "IDDISTRITO",
                            Integer.class
                    ),
                    nombres,
                    apePaterno,
                    apeMaterno,
                    text(rs, "EST_CIVIL"),
                    text(rs, "DNI"),
                    text(rs, "DIRECCION"),
                    text(rs, "CELULAR"),
                    rs.getObject(
                            "FECNAC",
                            LocalDate.class
                    ),
                    text(rs, "CORREO"),
                    text(rs, "ESTADO"),
                    text(rs, "NOMDISTRITO"),
                    text(rs, "NOMPROVINCIA"),
                    text(rs, "NOMDEPARTAMENTO"),
                    join(
                            nombres,
                            apePaterno,
                            apeMaterno
                    )
            );
        };
    }

    private static String join(String... values) {
        StringBuilder builder =
                new StringBuilder();

        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(value.trim());
        }

        return builder.toString();
    }

    private static String text(
            ResultSet rs,
            String column
    ) throws SQLException {
        String value = rs.getString(column);

        return value == null
                ? null
                : value.trim();
    }
}