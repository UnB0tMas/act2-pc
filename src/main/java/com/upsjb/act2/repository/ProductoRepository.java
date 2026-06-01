package com.upsjb.act2.repository;

import com.upsjb.act2.model.Producto;
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
import java.util.List;
import java.util.Optional;

@Repository
public class ProductoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ProductoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Producto> findPage(String texto, Integer idCategoria, String estado, int page, int size) {
        MapSqlParameterSource params = baseFilterParams(texto, idCategoria, estado);
        params.addValue("offset", Math.max(page, 0) * size);
        params.addValue("size", size);

        String sql = """
                SELECT
                    p.IDPRODUCTO,
                    p.IDCATEGORIA,
                    c.NOMCATEGORIA,
                    p.NOMPRODUCTO,
                    p.DESCRIPCION,
                    p.PRECIO,
                    p.MARCA,
                    p.ESTADO
                FROM PRODUCTO p
                LEFT JOIN CATEGORIA c ON c.IDCATEGORIA = p.IDCATEGORIA
                WHERE (:texto IS NULL OR UPPER(p.NOMPRODUCTO) LIKE :texto OR UPPER(p.MARCA) LIKE :texto)
                  AND (:idCategoria IS NULL OR p.IDCATEGORIA = :idCategoria)
                  AND (:estado IS NULL OR LTRIM(RTRIM(p.ESTADO)) = :estado)
                ORDER BY p.IDPRODUCTO DESC
                OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
                """;

        return jdbc.query(sql, params, productoMapper());
    }

    public long count(String texto, Integer idCategoria, String estado) {
        String sql = """
                SELECT COUNT(*)
                FROM PRODUCTO p
                WHERE (:texto IS NULL OR UPPER(p.NOMPRODUCTO) LIKE :texto OR UPPER(p.MARCA) LIKE :texto)
                  AND (:idCategoria IS NULL OR p.IDCATEGORIA = :idCategoria)
                  AND (:estado IS NULL OR LTRIM(RTRIM(p.ESTADO)) = :estado)
                """;

        Long total = jdbc.queryForObject(sql, baseFilterParams(texto, idCategoria, estado), Long.class);
        return total == null ? 0L : total;
    }

    public Optional<Producto> findById(Integer idProducto) {
        String sql = """
                SELECT
                    p.IDPRODUCTO,
                    p.IDCATEGORIA,
                    c.NOMCATEGORIA,
                    p.NOMPRODUCTO,
                    p.DESCRIPCION,
                    p.PRECIO,
                    p.MARCA,
                    p.ESTADO
                FROM PRODUCTO p
                LEFT JOIN CATEGORIA c ON c.IDCATEGORIA = p.IDCATEGORIA
                WHERE p.IDPRODUCTO = :idProducto
                """;

        Producto producto = DataAccessUtils.singleResult(
                jdbc.query(sql, new MapSqlParameterSource("idProducto", idProducto), productoMapper())
        );

        return Optional.ofNullable(producto);
    }

    public Integer insert(Producto producto) {
        String sql = """
                INSERT INTO PRODUCTO (IDCATEGORIA, NOMPRODUCTO, DESCRIPCION, PRECIO, MARCA, ESTADO)
                VALUES (:idCategoria, :nomProducto, :descripcion, :precio, :marca, :estado)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCategoria", producto.getIdCategoria())
                .addValue("nomProducto", producto.getNomProducto())
                .addValue("descripcion", producto.getDescripcion())
                .addValue("precio", producto.getPrecio())
                .addValue("marca", producto.getMarca())
                .addValue("estado", EstadoUtil.normalizar(producto.getEstado()));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"IDPRODUCTO"});

        Number key = keyHolder.getKey();
        return key == null ? null : key.intValue();
    }

    public int update(Producto producto) {
        String sql = """
                UPDATE PRODUCTO
                SET IDCATEGORIA = :idCategoria,
                    NOMPRODUCTO = :nomProducto,
                    DESCRIPCION = :descripcion,
                    PRECIO = :precio,
                    MARCA = :marca,
                    ESTADO = :estado
                WHERE IDPRODUCTO = :idProducto
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idProducto", producto.getIdProducto())
                .addValue("idCategoria", producto.getIdCategoria())
                .addValue("nomProducto", producto.getNomProducto())
                .addValue("descripcion", producto.getDescripcion())
                .addValue("precio", producto.getPrecio())
                .addValue("marca", producto.getMarca())
                .addValue("estado", EstadoUtil.normalizar(producto.getEstado()));

        return jdbc.update(sql, params);
    }

    public int changeEstado(Integer idProducto, String estado) {
        String sql = """
                UPDATE PRODUCTO
                SET ESTADO = :estado
                WHERE IDPRODUCTO = :idProducto
                """;

        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idProducto", idProducto)
                .addValue("estado", EstadoUtil.normalizar(estado)));
    }

    private MapSqlParameterSource baseFilterParams(String texto, Integer idCategoria, String estado) {
        String filtroTexto = texto == null || texto.isBlank()
                ? null
                : "%" + texto.trim().toUpperCase() + "%";

        return new MapSqlParameterSource()
                .addValue("texto", filtroTexto)
                .addValue("idCategoria", idCategoria)
                .addValue("estado", EstadoUtil.normalizarFiltro(estado));
    }

    private RowMapper<Producto> productoMapper() {
        return (rs, rowNum) -> new Producto(
                rs.getObject("IDPRODUCTO", Integer.class),
                rs.getObject("IDCATEGORIA", Integer.class),
                text(rs, "NOMCATEGORIA"),
                text(rs, "NOMPRODUCTO"),
                text(rs, "DESCRIPCION"),
                rs.getBigDecimal("PRECIO"),
                text(rs, "MARCA"),
                text(rs, "ESTADO")
        );
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}