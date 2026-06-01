package com.upsjb.act2.repository;

import com.upsjb.act2.model.Departamento;
import com.upsjb.act2.model.Distrito;
import com.upsjb.act2.model.Provincia;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UbigeoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public UbigeoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Departamento> findDepartamentosActivos() {
        String sql = """
                SELECT IDDepartamento, NOMDEPARTAMENTO, ESTADO
                FROM DEPARTAMENTO
                WHERE LTRIM(RTRIM(CAST(ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                ORDER BY NOMDEPARTAMENTO
                """;

        return jdbc.query(sql, departamentoMapper());
    }

    public List<Provincia> findProvinciasByDepartamento(Integer idDepartamento) {
        String sql = """
                SELECT
                    p.IDPROVINCIA,
                    p.IDDEPARTAMENTO,
                    p.NOMPROVINCIA,
                    p.ESTADO,
                    d.NOMDEPARTAMENTO
                FROM PROVINCIA p
                LEFT JOIN DEPARTAMENTO d ON d.IDDepartamento = p.IDDEPARTAMENTO
                WHERE p.IDDEPARTAMENTO = :idDepartamento
                  AND LTRIM(RTRIM(CAST(p.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                ORDER BY p.NOMPROVINCIA
                """;

        return jdbc.query(sql, new MapSqlParameterSource("idDepartamento", idDepartamento), provinciaMapper());
    }

    public List<Distrito> findDistritosByProvincia(Integer idProvincia) {
        String sql = """
                SELECT
                    dis.IDDISTRITO,
                    dis.IDPROVINCIA,
                    dis.NOMDISTRITO,
                    dis.ESTADO,
                    pro.IDDEPARTAMENTO,
                    pro.NOMPROVINCIA,
                    dep.NOMDEPARTAMENTO
                FROM DISTRITO dis
                LEFT JOIN PROVINCIA pro ON pro.IDPROVINCIA = dis.IDPROVINCIA
                LEFT JOIN DEPARTAMENTO dep ON dep.IDDepartamento = pro.IDDEPARTAMENTO
                WHERE dis.IDPROVINCIA = :idProvincia
                  AND LTRIM(RTRIM(CAST(dis.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                ORDER BY dis.NOMDISTRITO
                """;

        return jdbc.query(sql, new MapSqlParameterSource("idProvincia", idProvincia), distritoMapper());
    }

    public List<Distrito> findDistritosActivosConNombreCompleto() {
        String sql = """
                SELECT
                    dis.IDDISTRITO,
                    dis.IDPROVINCIA,
                    dis.NOMDISTRITO,
                    dis.ESTADO,
                    pro.IDDEPARTAMENTO,
                    pro.NOMPROVINCIA,
                    dep.NOMDEPARTAMENTO
                FROM DISTRITO dis
                LEFT JOIN PROVINCIA pro ON pro.IDPROVINCIA = dis.IDPROVINCIA
                LEFT JOIN DEPARTAMENTO dep ON dep.IDDepartamento = pro.IDDEPARTAMENTO
                WHERE LTRIM(RTRIM(CAST(dis.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                ORDER BY dep.NOMDEPARTAMENTO, pro.NOMPROVINCIA, dis.NOMDISTRITO
                """;

        return jdbc.query(sql, distritoMapper());
    }

    private RowMapper<Departamento> departamentoMapper() {
        return (rs, rowNum) -> new Departamento(
                rs.getObject("IDDepartamento", Integer.class),
                text(rs, "NOMDEPARTAMENTO"),
                text(rs, "ESTADO")
        );
    }

    private RowMapper<Provincia> provinciaMapper() {
        return (rs, rowNum) -> new Provincia(
                rs.getObject("IDPROVINCIA", Integer.class),
                rs.getObject("IDDEPARTAMENTO", Integer.class),
                text(rs, "NOMPROVINCIA"),
                text(rs, "ESTADO"),
                text(rs, "NOMDEPARTAMENTO")
        );
    }

    private RowMapper<Distrito> distritoMapper() {
        return (rs, rowNum) -> {
            String departamento = text(rs, "NOMDEPARTAMENTO");
            String provincia = text(rs, "NOMPROVINCIA");
            String distrito = text(rs, "NOMDISTRITO");

            return new Distrito(
                    rs.getObject("IDDISTRITO", Integer.class),
                    rs.getObject("IDPROVINCIA", Integer.class),
                    distrito,
                    text(rs, "ESTADO"),
                    rs.getObject("IDDEPARTAMENTO", Integer.class),
                    provincia,
                    departamento,
                    join(departamento, provincia, distrito)
            );
        };
    }

    private static String join(String... values) {
        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                if (!builder.isEmpty()) {
                    builder.append(" / ");
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