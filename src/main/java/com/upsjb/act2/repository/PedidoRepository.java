package com.upsjb.act2.repository;

import com.upsjb.act2.model.DetallePedido;
import com.upsjb.act2.model.Pedido;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class PedidoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PedidoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Pedido> findPage(LocalDate desde, LocalDate hasta, String estado, int page, int size) {
        MapSqlParameterSource params = filterParams(desde, hasta, estado);
        params.addValue("offset", Math.max(page, 0) * size);
        params.addValue("size", size);

        String sql = """
                SELECT
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
                WHERE (:desde IS NULL OR p.FECHA >= :desde)
                  AND (:hasta IS NULL OR p.FECHA <= :hasta)
                  AND (:estado IS NULL OR p.ESTADO = :estado)
                GROUP BY p.IDPEDIDO, p.IDMESA, p.FECHA, p.ESTADO, m.NUMPISO, m.NUNMESA
                ORDER BY p.IDPEDIDO DESC
                OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
                """;

        return jdbc.query(sql, params, pedidoMapper());
    }

    public long count(LocalDate desde, LocalDate hasta, String estado) {
        String sql = """
                SELECT COUNT(*)
                FROM PEDIDOS p
                WHERE (:desde IS NULL OR p.FECHA >= :desde)
                  AND (:hasta IS NULL OR p.FECHA <= :hasta)
                  AND (:estado IS NULL OR p.ESTADO = :estado)
                """;

        Long total = jdbc.queryForObject(sql, filterParams(desde, hasta, estado), Long.class);
        return total == null ? 0L : total;
    }

    public Optional<Pedido> findById(Integer idPedido) {
        String sql = """
                SELECT
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
                WHERE p.IDPEDIDO = :idPedido
                GROUP BY p.IDPEDIDO, p.IDMESA, p.FECHA, p.ESTADO, m.NUMPISO, m.NUNMESA
                """;

        Pedido pedido = DataAccessUtils.singleResult(
                jdbc.query(sql, new MapSqlParameterSource("idPedido", idPedido), pedidoMapper())
        );

        return Optional.ofNullable(pedido);
    }

    public List<DetallePedido> findDetalleByPedido(Integer idPedido) {
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
                FROM DETALLE_PEDIDO dp
                LEFT JOIN PRODUCTO pr ON pr.IDPRODUCTO = dp.IDPRODUCTO
                LEFT JOIN AREA a ON a.IDAREA = dp.IDAREA
                LEFT JOIN USUARIO u ON u.IDUSUARIO = dp.IDUSUARIO
                WHERE dp.IDPEDIDO = :idPedido
                ORDER BY dp.IDDETALLE_PEDIDO ASC
                """;

        return jdbc.query(sql, new MapSqlParameterSource("idPedido", idPedido), detalleMapper());
    }

    private MapSqlParameterSource filterParams(LocalDate desde, LocalDate hasta, String estado) {
        return new MapSqlParameterSource()
                .addValue("desde", desde)
                .addValue("hasta", hasta)
                .addValue("estado", estado == null || estado.isBlank() ? null : estado.trim());
    }

    private RowMapper<Pedido> pedidoMapper() {
        return (rs, rowNum) -> {
            String piso = text(rs, "NUMPISO");
            String mesa = text(rs, "NUNMESA");
            BigDecimal total = rs.getBigDecimal("TOTAL");

            return new Pedido(
                    rs.getObject("IDPEDIDO", Integer.class),
                    rs.getObject("IDMESA", Integer.class),
                    rs.getObject("FECHA", LocalDate.class),
                    text(rs, "ESTADO"),
                    piso,
                    mesa,
                    "Piso " + safe(piso) + " - Mesa " + safe(mesa),
                    rs.getObject("TOTAL_ITEMS", Integer.class),
                    total == null ? BigDecimal.ZERO : total
            );
        };
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}