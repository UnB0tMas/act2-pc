package com.upsjb.act2.repository;

import com.upsjb.act2.model.DashboardResumen;
import com.upsjb.act2.model.Pedido;
import com.upsjb.act2.model.Venta;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class DashboardRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DashboardRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public DashboardResumen getResumen() {
        return new DashboardResumen(
                count("SELECT COUNT(*) FROM PRODUCTO WHERE LTRIM(RTRIM(ESTADO)) IN ('1', 'A')"),
                count("SELECT COUNT(*) FROM CLIENTE WHERE LTRIM(RTRIM(ESTADO)) IN ('1', 'A')"),
                count("SELECT COUNT(*) FROM EMPLEADO WHERE LTRIM(RTRIM(ESTADO)) IN ('1', 'A')"),
                count("SELECT COUNT(*) FROM VENTAS"),
                count("SELECT COUNT(*) FROM PEDIDOS"),
                count("SELECT COUNT(*) FROM MESAS"),
                findUltimasVentas(5),
                findUltimosPedidos(5)
        );
    }

    public List<Venta> findUltimasVentas(int limit) {
        int top = Math.max(1, Math.min(limit, 20));

        String sql = """
                SELECT TOP %d
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
                        NULLIF(LTRIM(RTRIM(CONCAT(p.NOMBRES, ' ', p.APEPATERNO, ' ', p.APEMATERNO))), ''),
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
                ORDER BY v.IDVENTAS DESC
                """.formatted(top);

        return jdbc.query(sql, ventaMapper());
    }

    public List<Pedido> findUltimosPedidos(int limit) {
        int top = Math.max(1, Math.min(limit, 20));

        String sql = """
                SELECT TOP %d
                    p.IDPEDIDO,
                    p.IDMESA,
                    p.FECHA,
                    p.ESTADO,
                    m.NUMPISO,
                    m.NUNMESA,
                    COUNT(dp.IDDETALLE_PEDIDO) AS TOTAL_ITEMS,
                    COALESCE(SUM(COALESCE(dp.CANTIDAD, 0) * COALESCE(dp.PRECIO, 0)), 0) AS TOTAL
                FROM PEDIDOS p
                LEFT JOIN MESAS m ON m.IDMESA = p.IDMESA
                LEFT JOIN DETALLE_PEDIDO dp ON dp.IDPEDIDO = p.IDPEDIDO
                GROUP BY p.IDPEDIDO, p.IDMESA, p.FECHA, p.ESTADO, m.NUMPISO, m.NUNMESA
                ORDER BY p.IDPEDIDO DESC
                """.formatted(top);

        return jdbc.query(sql, pedidoMapper());
    }

    private long count(String sql) {
        Long value = jdbc.queryForObject(sql, Map.of(), Long.class);
        return value == null ? 0L : value;
    }

    private RowMapper<Venta> ventaMapper() {
        return (rs, rowNum) -> new Venta(
                rs.getObject("IDVENTAS", Integer.class),
                rs.getObject("IDCLIENTE", Integer.class),
                rs.getObject("IDMETODO_PAGO", Integer.class),
                rs.getObject("IDPEDIDO", Integer.class),
                rs.getObject("IDUSUARIO", Integer.class),
                rs.getObject("FECHAVENTA", java.time.LocalDate.class),
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

    private RowMapper<Pedido> pedidoMapper() {
        return (rs, rowNum) -> {
            String piso = text(rs, "NUMPISO");
            String mesa = text(rs, "NUNMESA");
            BigDecimal total = rs.getBigDecimal("TOTAL");

            return new Pedido(
                    rs.getObject("IDPEDIDO", Integer.class),
                    rs.getObject("IDMESA", Integer.class),
                    rs.getObject("FECHA", java.time.LocalDate.class),
                    text(rs, "ESTADO"),
                    piso,
                    mesa,
                    "Piso " + safe(piso) + " - Mesa " + safe(mesa),
                    rs.getObject("TOTAL_ITEMS", Integer.class),
                    total == null ? BigDecimal.ZERO : total
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