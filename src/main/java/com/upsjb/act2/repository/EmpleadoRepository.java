package com.upsjb.act2.repository;

import com.upsjb.act2.model.Empleado;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class EmpleadoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EmpleadoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Empleado> findPage(String texto, Integer idCargo, Integer idContrato, String estado, int page, int size) {
        MapSqlParameterSource params = filterParams(texto, idCargo, idContrato, estado);
        params.addValue("offset", Math.max(page, 0) * size);
        params.addValue("size", size);

        String sql = """
                SELECT
                    e.IDEMPLEADO,
                    e.IDPERSONA,
                    e.IDCONTRATO,
                    e.IDCARGO,
                    e.SALARIO,
                    e.TURNO,
                    e.ESTADO,
                    p.DNI,
                    p.NOMBRES,
                    p.APEPATERNO,
                    p.APEMATERNO,
                    p.CORREO,
                    p.CELULAR,
                    c.NOMCARGO,
                    co.NOMCONTRATO
                FROM EMPLEADO e
                LEFT JOIN PERSONA p ON p.IDPERSONA = e.IDPERSONA
                LEFT JOIN CARGO c ON c.IDCARGO = e.IDCARGO
                LEFT JOIN CONTRATO co ON co.IDCONTRATO = e.IDCONTRATO
                WHERE (:texto IS NULL
                        OR UPPER(p.DNI) LIKE :texto
                        OR UPPER(p.NOMBRES) LIKE :texto
                        OR UPPER(p.APEPATERNO) LIKE :texto
                        OR UPPER(p.APEMATERNO) LIKE :texto
                        OR UPPER(p.CORREO) LIKE :texto)
                  AND (:idCargo IS NULL OR e.IDCARGO = :idCargo)
                  AND (:idContrato IS NULL OR e.IDCONTRATO = :idContrato)
                  AND (:estado IS NULL OR e.ESTADO = :estado)
                ORDER BY e.IDEMPLEADO DESC
                OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
                """;

        return jdbc.query(sql, params, empleadoMapper());
    }

    public long count(String texto, Integer idCargo, Integer idContrato, String estado) {
        String sql = """
                SELECT COUNT(*)
                FROM EMPLEADO e
                LEFT JOIN PERSONA p ON p.IDPERSONA = e.IDPERSONA
                WHERE (:texto IS NULL
                        OR UPPER(p.DNI) LIKE :texto
                        OR UPPER(p.NOMBRES) LIKE :texto
                        OR UPPER(p.APEPATERNO) LIKE :texto
                        OR UPPER(p.APEMATERNO) LIKE :texto
                        OR UPPER(p.CORREO) LIKE :texto)
                  AND (:idCargo IS NULL OR e.IDCARGO = :idCargo)
                  AND (:idContrato IS NULL OR e.IDCONTRATO = :idContrato)
                  AND (:estado IS NULL OR e.ESTADO = :estado)
                """;

        Long total = jdbc.queryForObject(sql, filterParams(texto, idCargo, idContrato, estado), Long.class);
        return total == null ? 0L : total;
    }

    public Optional<Empleado> findById(Integer idEmpleado) {
        String sql = """
                SELECT
                    e.IDEMPLEADO,
                    e.IDPERSONA,
                    e.IDCONTRATO,
                    e.IDCARGO,
                    e.SALARIO,
                    e.TURNO,
                    e.ESTADO,
                    p.DNI,
                    p.NOMBRES,
                    p.APEPATERNO,
                    p.APEMATERNO,
                    p.CORREO,
                    p.CELULAR,
                    c.NOMCARGO,
                    co.NOMCONTRATO
                FROM EMPLEADO e
                LEFT JOIN PERSONA p ON p.IDPERSONA = e.IDPERSONA
                LEFT JOIN CARGO c ON c.IDCARGO = e.IDCARGO
                LEFT JOIN CONTRATO co ON co.IDCONTRATO = e.IDCONTRATO
                WHERE e.IDEMPLEADO = :idEmpleado
                """;

        Empleado empleado = DataAccessUtils.singleResult(
                jdbc.query(sql, new MapSqlParameterSource("idEmpleado", idEmpleado), empleadoMapper())
        );

        return Optional.ofNullable(empleado);
    }

    public Integer insert(Empleado empleado) {
        String sql = """
                INSERT INTO EMPLEADO (IDPERSONA, IDCONTRATO, IDCARGO, SALARIO, TURNO, ESTADO)
                VALUES (:idPersona, :idContrato, :idCargo, :salario, :turno, :estado)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params(empleado), keyHolder, new String[]{"IDEMPLEADO"});

        Number key = keyHolder.getKey();
        return key == null ? null : key.intValue();
    }

    public int update(Empleado empleado) {
        String sql = """
                UPDATE EMPLEADO
                SET IDPERSONA = :idPersona,
                    IDCONTRATO = :idContrato,
                    IDCARGO = :idCargo,
                    SALARIO = :salario,
                    TURNO = :turno,
                    ESTADO = :estado
                WHERE IDEMPLEADO = :idEmpleado
                """;

        return jdbc.update(sql, params(empleado).addValue("idEmpleado", empleado.getIdEmpleado()));
    }

    public int changeEstado(Integer idEmpleado, String estado) {
        String sql = """
                UPDATE EMPLEADO
                SET ESTADO = :estado
                WHERE IDEMPLEADO = :idEmpleado
                """;

        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idEmpleado", idEmpleado)
                .addValue("estado", estado));
    }

    private MapSqlParameterSource params(Empleado empleado) {
        return new MapSqlParameterSource()
                .addValue("idPersona", empleado.getIdPersona())
                .addValue("idContrato", empleado.getIdContrato())
                .addValue("idCargo", empleado.getIdCargo())
                .addValue("salario", empleado.getSalario())
                .addValue("turno", empleado.getTurno())
                .addValue("estado", empleado.getEstado() == null || empleado.getEstado().isBlank() ? "A" : empleado.getEstado().trim());
    }

    private MapSqlParameterSource filterParams(String texto, Integer idCargo, Integer idContrato, String estado) {
        return new MapSqlParameterSource()
                .addValue("texto", texto == null || texto.isBlank() ? null : "%" + texto.trim().toUpperCase() + "%")
                .addValue("idCargo", idCargo)
                .addValue("idContrato", idContrato)
                .addValue("estado", estado == null || estado.isBlank() ? null : estado.trim());
    }

    private RowMapper<Empleado> empleadoMapper() {
        return (rs, rowNum) -> {
            String nombres = text(rs, "NOMBRES");
            String apePaterno = text(rs, "APEPATERNO");
            String apeMaterno = text(rs, "APEMATERNO");

            return new Empleado(
                    rs.getObject("IDEMPLEADO", Integer.class),
                    rs.getObject("IDPERSONA", Integer.class),
                    rs.getObject("IDCONTRATO", Integer.class),
                    rs.getObject("IDCARGO", Integer.class),
                    rs.getBigDecimal("SALARIO"),
                    text(rs, "TURNO"),
                    text(rs, "ESTADO"),
                    text(rs, "DNI"),
                    nombres,
                    apePaterno,
                    apeMaterno,
                    text(rs, "CORREO"),
                    text(rs, "CELULAR"),
                    text(rs, "NOMCARGO"),
                    text(rs, "NOMCONTRATO"),
                    join(nombres, apePaterno, apeMaterno)
            );
        };
    }

    private static String join(String... values) {
        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(value.trim());
            }
        }

        return builder.toString();
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}