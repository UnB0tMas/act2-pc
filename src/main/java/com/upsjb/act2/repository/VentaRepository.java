package com.upsjb.act2.repository;

import com.upsjb.act2.model.DetallePedido;
import com.upsjb.act2.model.Venta;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class VentaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public VentaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Venta> findPage(LocalDate desde, LocalDate hasta, Integer idMetodoPago, String estado, int page, int size) {
        MapSqlParameterSource params = filterParams(desde, hasta, idMetodoPago, estado);
        params.addValue("offset", Math.max(page, 0) * size);
        params.addValue("size", size);

        String sql = """
                SELECT
                    v.IDVENTAS,
                    v.IDCLIENTE,
                    v.IDMETODO_PAGO,
                    v.IDPEDIDO,
                    v.IDUSUARIO,
                    v.FECHAVENTA,
                    v.DOCUMENTO,
                    v.MONTOTOTAL,
                    v.DESCUENTO,
                    v.SUBTOTOTAL,
                    v.IGV,
                    v.TOTALPAGAR,
                    v.ESTADO,
                    COALESCE(
                        CONCAT(p.NOMBRES, ' ', p.APEPATERNO, ' ', p.APEMATERNO),
                        e.RAZON_SOCIAL,
                        'Cliente no definido'
                    ) AS CLIENTE_NOMBRE,
                    COALESCE(p.DNI, e.RUC, '') AS CLIENTE_DOCUMENTO,
                    mp.NOMMETODO,
                    u.LOGEO
                FROM VENTAS v
                LEFT JOIN CLIENTE c ON c.IDCLIENTE = v.IDCLIENTE
                LEFT JOIN PERSONA p ON p.IDPERSONA = c.IDPERSONA
                LEFT JOIN EMPRESA e ON e.IDEMPRESA = c.IDEMPRESA
                LEFT JOIN METODO_PAGO mp ON mp.IDMETODO_PAGO = v.IDMETODO_PAGO
                LEFT JOIN USUARIO u ON u.IDUSUARIO = v.IDUSUARIO
                WHERE (:desde IS NULL OR v.FECHAVENTA >= :desde)
                  AND (:hasta IS NULL OR v.FECHAVENTA <= :hasta)
                  AND (:idMetodoPago IS NULL OR v.IDMETODO_PAGO = :idMetodoPago)
                  AND (:estado IS NULL OR v.ESTADO = :estado)
                ORDER BY v.IDVENTAS DESC
                OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
                """;

        return jdbc.query(sql, params, ventaMapper());
    }

    public long count(LocalDate desde, LocalDate hasta, Integer idMetodoPago, String estado) {
        String sql = """
                SELECT COUNT(*)
                FROM VENTAS v
                WHERE (:desde IS NULL OR v.FECHAVENTA >= :desde)
                  AND (:hasta IS NULL OR v.FECHAVENTA <= :hasta)
                  AND (:idMetodoPago IS NULL OR v.IDMETODO_PAGO = :idMetodoPago)
                  AND (:estado IS NULL OR v.ESTADO = :estado)
                """;

        Long total = jdbc.queryForObject(sql, filterParams(desde, hasta, idMetodoPago, estado), Long.class);
        return total == null ? 0L : total;
    }

    public Optional<Venta> findById(Integer idVentas) {
        String sql = """
                SELECT
                    v.IDVENTAS,
                    v.IDCLIENTE,
                    v.IDMETODO_PAGO,
                    v.IDPEDIDO,
                    v.IDUSUARIO,
                    v.FECHAVENTA,
                    v.DOCUMENTO,
                    v.MONTOTOTAL,
                    v.DESCUENTO,
                    v.SUBTOTOTAL,
                    v.IGV,
                    v.TOTALPAGAR,
                    v.ESTADO,
                    COALESCE(
                        CONCAT(p.NOMBRES, ' ', p.APEPATERNO, ' ', p.APEMATERNO),
                        e.RAZON_SOCIAL,
                        'Cliente no definido'
                    ) AS CLIENTE_NOMBRE,
                    COALESCE(p.DNI, e.RUC, '') AS CLIENTE_DOCUMENTO,
                    mp.NOMMETODO,
                    u.LOGEO
                FROM VENTAS v
                LEFT JOIN CLIENTE c ON c.IDCLIENTE = v.IDCLIENTE
                LEFT JOIN PERSONA p ON p.IDPERSONA = c.IDPERSONA
                LEFT JOIN EMPRESA e ON e.IDEMPRESA = c.IDEMPRESA
                LEFT JOIN METODO_PAGO mp ON mp.IDMETODO_PAGO = v.IDMETODO_PAGO
                LEFT JOIN USUARIO u ON u.IDUSUARIO = v.IDUSUARIO
                WHERE v.IDVENTAS = :idVentas
                """;

        Venta venta = DataAccessUtils.singleResult(
                jdbc.query(sql, new MapSqlParameterSource("idVentas", idVentas), ventaMapper())
        );

        return Optional.ofNullable(venta);
    }

    public List<DetallePedido> findDetalleByVenta(Integer idVentas) {
        String sql = """
                SELECT
                    dp.IDDETALLE_PEDIDO,
                    dp.IDPEDIDO,
                    dp.IDPRODUCTO,
                    dp.IDAREA,
                    dp.IDUSUARIO,
                    dp.CANTIDAD,
                    dp.PRECIO,
                    dp.ESTADO,
                    pr.NOMPRODUCTO,
                    a.NOMAREA,
                    u.LOGEO,
                    COALESCE(dp.CANTIDAD, 0) * COALESCE(dp.PRECIO, 0) AS SUBTOTAL
                FROM VENTAS v
                INNER JOIN DETALLE_PEDIDO dp ON dp.IDPEDIDO = v.IDPEDIDO
                LEFT JOIN PRODUCTO pr ON pr.IDPRODUCTO = dp.IDPRODUCTO
                LEFT JOIN AREA a ON a.IDAREA = dp.IDAREA
                LEFT JOIN USUARIO u ON u.IDUSUARIO = dp.IDUSUARIO
                WHERE v.IDVENTAS = :idVentas
                ORDER BY dp.IDDETALLE_PEDIDO ASC
                """;

        return jdbc.query(sql, new MapSqlParameterSource("idVentas", idVentas), detalleMapper());
    }

    private MapSqlParameterSource filterParams(LocalDate desde, LocalDate hasta, Integer idMetodoPago, String estado) {
        return new MapSqlParameterSource()
                .addValue("desde", desde)
                .addValue("hasta", hasta)
                .addValue("idMetodoPago", idMetodoPago)
                .addValue("estado", estado == null || estado.isBlank() ? null : estado.trim());
    }

    private RowMapper<Venta> ventaMapper() {
        return (rs, rowNum) -> new Venta(
                rs.getObject("IDVENTAS", Integer.class),
                rs.getObject("IDCLIENTE", Integer.class),
                rs.getObject("IDMETODO_PAGO", Integer.class),
                rs.getObject("IDPEDIDO", Integer.class),
                rs.getObject("IDUSUARIO", Integer.class),
                rs.getObject("FECHAVENTA", LocalDate.class),
                text(rs, "DOCUMENTO"),
                rs.getBigDecimal("MONTOTOTAL"),
                rs.getBigDecimal("DESCUENTO"),
                rs.getBigDecimal("SUBTOTOTAL"),
                rs.getBigDecimal("IGV"),
                rs.getBigDecimal("TOTALPAGAR"),
                text(rs, "ESTADO"),
                text(rs, "CLIENTE_NOMBRE"),
                text(rs, "CLIENTE_DOCUMENTO"),
                text(rs, "NOMMETODO"),
                text(rs, "LOGEO")
        );
    }

    private RowMapper<DetallePedido> detalleMapper() {
        return (rs, rowNum) -> new DetallePedido(
                rs.getObject("IDDETALLE_PEDIDO", Integer.class),
                rs.getObject("IDPEDIDO", Integer.class),
                rs.getObject("IDPRODUCTO", Integer.class),
                rs.getObject("IDAREA", Integer.class),
                rs.getObject("IDUSUARIO", Integer.class),
                rs.getObject("CANTIDAD", Integer.class),
                rs.getBigDecimal("PRECIO"),
                text(rs, "ESTADO"),
                text(rs, "NOMPRODUCTO"),
                text(rs, "NOMAREA"),
                text(rs, "LOGEO"),
                rs.getBigDecimal("SUBTOTAL")
        );
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}