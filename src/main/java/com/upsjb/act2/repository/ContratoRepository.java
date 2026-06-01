package com.upsjb.act2.repository;

import com.upsjb.act2.model.Contrato;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ContratoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ContratoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Contrato> findAll() {
        String sql = """
                SELECT IDCONTRATO, NOMCONTRATO, ESTADO
                FROM CONTRATO
                ORDER BY NOMCONTRATO
                """;
        return jdbc.query(sql, contratoMapper());
    }

    public List<Contrato> findAllActivos() {
        String sql = """
                SELECT IDCONTRATO, NOMCONTRATO, ESTADO
                FROM CONTRATO
                WHERE LTRIM(RTRIM(ESTADO)) IN ('1', 'A')
                ORDER BY NOMCONTRATO
                """;
        return jdbc.query(sql, contratoMapper());
    }

    private RowMapper<Contrato> contratoMapper() {
        return (rs, rowNum) -> new Contrato(
                rs.getObject("IDCONTRATO", Integer.class),
                text(rs, "NOMCONTRATO"),
                text(rs, "ESTADO")
        );
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}