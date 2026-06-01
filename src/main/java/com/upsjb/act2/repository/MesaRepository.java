package com.upsjb.act2.repository;

import com.upsjb.act2.model.Mesa;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MesaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public MesaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Mesa> findAll() {
        String sql = """
                SELECT IDMESA, NUMPISO, NUNMESA, ESTADO
                FROM MESAS
                ORDER BY TRY_CONVERT(INT, LTRIM(RTRIM(NUMPISO))), TRY_CONVERT(INT, LTRIM(RTRIM(NUNMESA)))
                """;
        return jdbc.query(sql, mesaMapper());
    }

    public List<Mesa> findAllActivas() {
        String sql = """
                SELECT IDMESA, NUMPISO, NUNMESA, ESTADO
                FROM MESAS
                WHERE LTRIM(RTRIM(CAST(ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                ORDER BY TRY_CONVERT(INT, LTRIM(RTRIM(NUMPISO))), TRY_CONVERT(INT, LTRIM(RTRIM(NUNMESA)))
                """;
        return jdbc.query(sql, mesaMapper());
    }

    private RowMapper<Mesa> mesaMapper() {
        return (rs, rowNum) -> {
            String piso = text(rs, "NUMPISO");
            String mesa = text(rs, "NUNMESA");

            return new Mesa(
                    rs.getObject("IDMESA", Integer.class),
                    piso,
                    mesa,
                    text(rs, "ESTADO"),
                    "Piso " + safe(piso) + " - Mesa " + safe(mesa)
            );
        };
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}