package com.upsjb.act2.repository;

import com.upsjb.act2.model.Area;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AreaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AreaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Area> findAll() {
        String sql = """
                SELECT IDAREA, NOMAREA, ESTADO
                FROM AREA
                ORDER BY NOMAREA
                """;
        return jdbc.query(sql, areaMapper());
    }

    public List<Area> findAllActivas() {
        String sql = """
                SELECT IDAREA, NOMAREA, ESTADO
                FROM AREA
                WHERE LTRIM(RTRIM(ESTADO)) IN ('1', 'A')
                ORDER BY NOMAREA
                """;
        return jdbc.query(sql, areaMapper());
    }

    private RowMapper<Area> areaMapper() {
        return (rs, rowNum) -> new Area(
                rs.getObject("IDAREA", Integer.class),
                text(rs, "NOMAREA"),
                text(rs, "ESTADO")
        );
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}