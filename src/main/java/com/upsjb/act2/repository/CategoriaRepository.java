package com.upsjb.act2.repository;

import com.upsjb.act2.model.Categoria;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class CategoriaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CategoriaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Categoria> findAll() {
        String sql = """
                SELECT IDCATEGORIA, NOMCATEGORIA, ESTADO
                FROM CATEGORIA
                ORDER BY NOMCATEGORIA
                """;
        return jdbc.query(sql, categoriaMapper());
    }

    public List<Categoria> findAllActivas() {
        String sql = """
                SELECT IDCATEGORIA, NOMCATEGORIA, ESTADO
                FROM CATEGORIA
                WHERE LTRIM(RTRIM(ESTADO)) IN ('1', 'A')
                ORDER BY NOMCATEGORIA
                """;
        return jdbc.query(sql, categoriaMapper());
    }

    public Optional<Categoria> findById(Integer idCategoria) {
        String sql = """
                SELECT IDCATEGORIA, NOMCATEGORIA, ESTADO
                FROM CATEGORIA
                WHERE IDCATEGORIA = :idCategoria
                """;

        Categoria categoria = DataAccessUtils.singleResult(
                jdbc.query(sql, new MapSqlParameterSource("idCategoria", idCategoria), categoriaMapper())
        );

        return Optional.ofNullable(categoria);
    }

    private RowMapper<Categoria> categoriaMapper() {
        return (rs, rowNum) -> new Categoria(
                rs.getObject("IDCATEGORIA", Integer.class),
                text(rs, "NOMCATEGORIA"),
                text(rs, "ESTADO")
        );
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}