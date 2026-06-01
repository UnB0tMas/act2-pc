package com.upsjb.act2.repository;

import com.upsjb.act2.model.Empresa;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class EmpresaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EmpresaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Empresa> findById(Integer idEmpresa) {
        String sql = """
                SELECT IDEMPRESA, RUC, RAZON_SOCIAL, DIRECCION, TELEFONO, ESTADO
                FROM EMPRESA
                WHERE IDEMPRESA = :idEmpresa
                """;

        Empresa empresa = DataAccessUtils.singleResult(
                jdbc.query(sql, new MapSqlParameterSource("idEmpresa", idEmpresa), empresaMapper())
        );

        return Optional.ofNullable(empresa);
    }

    public boolean existsByRuc(String ruc) {
        String sql = """
                SELECT COUNT(*)
                FROM EMPRESA
                WHERE RUC = :ruc
                """;

        Long total = jdbc.queryForObject(sql, new MapSqlParameterSource("ruc", ruc), Long.class);
        return total != null && total > 0;
    }

    public Integer insert(Empresa empresa) {
        String sql = """
                INSERT INTO EMPRESA (RUC, RAZON_SOCIAL, DIRECCION, TELEFONO, ESTADO)
                VALUES (:ruc, :razonSocial, :direccion, :telefono, :estado)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params(empresa), keyHolder, new String[]{"IDEMPRESA"});

        Number key = keyHolder.getKey();
        return key == null ? null : key.intValue();
    }

    public int update(Empresa empresa) {
        String sql = """
                UPDATE EMPRESA
                SET RUC = :ruc,
                    RAZON_SOCIAL = :razonSocial,
                    DIRECCION = :direccion,
                    TELEFONO = :telefono,
                    ESTADO = :estado
                WHERE IDEMPRESA = :idEmpresa
                """;

        return jdbc.update(sql, params(empresa).addValue("idEmpresa", empresa.getIdEmpresa()));
    }

    private MapSqlParameterSource params(Empresa empresa) {
        return new MapSqlParameterSource()
                .addValue("ruc", empresa.getRuc())
                .addValue("razonSocial", empresa.getRazonSocial())
                .addValue("direccion", empresa.getDireccion())
                .addValue("telefono", empresa.getTelefono())
                .addValue("estado", empresa.getEstado() == null || empresa.getEstado().isBlank() ? "A" : empresa.getEstado().trim());
    }

    private RowMapper<Empresa> empresaMapper() {
        return (rs, rowNum) -> new Empresa(
                rs.getObject("IDEMPRESA", Integer.class),
                text(rs, "RUC"),
                text(rs, "RAZON_SOCIAL"),
                text(rs, "DIRECCION"),
                text(rs, "TELEFONO"),
                text(rs, "ESTADO")
        );
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}