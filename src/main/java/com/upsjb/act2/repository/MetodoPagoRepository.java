package com.upsjb.act2.repository;

import com.upsjb.act2.model.MetodoPago;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MetodoPagoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public MetodoPagoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MetodoPago> findAll() {
        String sql = """
                SELECT IDMETODO_PAGO, NOMMETODO, ESTADO
                FROM METODO_PAGO
                ORDER BY NOMMETODO
                """;
        return jdbc.query(sql, metodoPagoMapper());
    }

    public List<MetodoPago> findAllActivos() {
        String sql = """
                SELECT IDMETODO_PAGO, NOMMETODO, ESTADO
                FROM METODO_PAGO
                WHERE LTRIM(RTRIM(ESTADO)) IN ('1', 'A')
                ORDER BY NOMMETODO
                """;
        return jdbc.query(sql, metodoPagoMapper());
    }

    private RowMapper<MetodoPago> metodoPagoMapper() {
        return (rs, rowNum) -> new MetodoPago(
                rs.getObject("IDMETODO_PAGO", Integer.class),
                text(rs, "NOMMETODO"),
                text(rs, "ESTADO")
        );
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}