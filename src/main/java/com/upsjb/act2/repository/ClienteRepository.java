package com.upsjb.act2.repository;

import com.upsjb.act2.model.Cliente;
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
public class ClienteRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ClienteRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }

    public List<Cliente> findPage(
            String texto,
            String tipo,
            String estado,
            int page,
            int size
    ) {
        MapSqlParameterSource params =
                filterParams(texto, tipo, estado);

        params.addValue(
                "offset",
                Math.max(page, 0) * size
        );

        params.addValue("size", size);

        String sql = """
                SELECT
                    c.IDCLIENTE,
                    c.IDPERSONA,
                    c.IDEMPRESA,
                    c.ESTADO,
                    CASE
                        WHEN c.IDPERSONA IS NOT NULL
                            THEN 'PERSONA'
                        WHEN c.IDEMPRESA IS NOT NULL
                            THEN 'EMPRESA'
                        ELSE 'SIN_TIPO'
                    END AS TIPO_CLIENTE,
                    p.DNI,
                    p.NOMBRES,
                    p.APEPATERNO,
                    p.APEMATERNO,
                    p.CORREO,
                    p.CELULAR,
                    e.RUC,
                    e.RAZON_SOCIAL,
                    e.TELEFONO
                FROM CLIENTE c
                LEFT JOIN PERSONA p
                    ON p.IDPERSONA = c.IDPERSONA
                LEFT JOIN EMPRESA e
                    ON e.IDEMPRESA = c.IDEMPRESA
                WHERE (
                    :estado IS NULL
                    OR LTRIM(
                        RTRIM(
                            CAST(c.ESTADO AS VARCHAR(10))
                        )
                    ) = :estado
                )
                AND (
                    :tipo IS NULL
                    OR (
                        :tipo = 'PERSONA'
                        AND c.IDPERSONA IS NOT NULL
                    )
                    OR (
                        :tipo = 'EMPRESA'
                        AND c.IDEMPRESA IS NOT NULL
                    )
                )
                AND (
                    :texto IS NULL
                    OR UPPER(p.DNI) LIKE :texto
                    OR UPPER(p.NOMBRES) LIKE :texto
                    OR UPPER(p.APEPATERNO) LIKE :texto
                    OR UPPER(p.APEMATERNO) LIKE :texto
                    OR UPPER(p.CORREO) LIKE :texto
                    OR UPPER(e.RUC) LIKE :texto
                    OR UPPER(e.RAZON_SOCIAL) LIKE :texto
                )
                ORDER BY c.IDCLIENTE DESC
                OFFSET :offset ROWS
                FETCH NEXT :size ROWS ONLY
                """;

        return jdbc.query(
                sql,
                params,
                clienteMapper()
        );
    }

    public long count(
            String texto,
            String tipo,
            String estado
    ) {
        String sql = """
                SELECT COUNT(*)
                FROM CLIENTE c
                LEFT JOIN PERSONA p
                    ON p.IDPERSONA = c.IDPERSONA
                LEFT JOIN EMPRESA e
                    ON e.IDEMPRESA = c.IDEMPRESA
                WHERE (
                    :estado IS NULL
                    OR LTRIM(
                        RTRIM(
                            CAST(c.ESTADO AS VARCHAR(10))
                        )
                    ) = :estado
                )
                AND (
                    :tipo IS NULL
                    OR (
                        :tipo = 'PERSONA'
                        AND c.IDPERSONA IS NOT NULL
                    )
                    OR (
                        :tipo = 'EMPRESA'
                        AND c.IDEMPRESA IS NOT NULL
                    )
                )
                AND (
                    :texto IS NULL
                    OR UPPER(p.DNI) LIKE :texto
                    OR UPPER(p.NOMBRES) LIKE :texto
                    OR UPPER(p.APEPATERNO) LIKE :texto
                    OR UPPER(p.APEMATERNO) LIKE :texto
                    OR UPPER(p.CORREO) LIKE :texto
                    OR UPPER(e.RUC) LIKE :texto
                    OR UPPER(e.RAZON_SOCIAL) LIKE :texto
                )
                """;

        Long total = jdbc.queryForObject(
                sql,
                filterParams(texto, tipo, estado),
                Long.class
        );

        return total == null
                ? 0L
                : total;
    }

    public Optional<Cliente> findById(
            Integer idCliente
    ) {
        String sql = """
                SELECT
                    c.IDCLIENTE,
                    c.IDPERSONA,
                    c.IDEMPRESA,
                    c.ESTADO,
                    CASE
                        WHEN c.IDPERSONA IS NOT NULL
                            THEN 'PERSONA'
                        WHEN c.IDEMPRESA IS NOT NULL
                            THEN 'EMPRESA'
                        ELSE 'SIN_TIPO'
                    END AS TIPO_CLIENTE,
                    p.DNI,
                    p.NOMBRES,
                    p.APEPATERNO,
                    p.APEMATERNO,
                    p.CORREO,
                    p.CELULAR,
                    e.RUC,
                    e.RAZON_SOCIAL,
                    e.TELEFONO
                FROM CLIENTE c
                LEFT JOIN PERSONA p
                    ON p.IDPERSONA = c.IDPERSONA
                LEFT JOIN EMPRESA e
                    ON e.IDEMPRESA = c.IDEMPRESA
                WHERE c.IDCLIENTE = :idCliente
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idCliente",
                                idCliente
                        );

        Cliente cliente =
                DataAccessUtils.singleResult(
                        jdbc.query(
                                sql,
                                params,
                                clienteMapper()
                        )
                );

        return Optional.ofNullable(cliente);
    }

    public Integer insertClientePersona(
            Integer idPersona,
            String estado
    ) {
        if (idPersona == null) {
            throw new IllegalArgumentException(
                    "El IDPERSONA es obligatorio "
                            + "para crear el cliente."
            );
        }

        String sql = """
                INSERT INTO CLIENTE (
                    IDPERSONA,
                    IDEMPRESA,
                    ESTADO
                )
                VALUES (
                    :idPersona,
                    NULL,
                    :estado
                )
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idPersona",
                                idPersona
                        )
                        .addValue(
                                "estado",
                                EstadoUtil.normalizar(estado)
                        );

        KeyHolder keyHolder =
                new GeneratedKeyHolder();

        int filasAfectadas = jdbc.update(
                sql,
                params,
                keyHolder,
                new String[]{"IDCLIENTE"}
        );

        if (filasAfectadas != 1) {
            throw new IllegalStateException(
                    "No se pudo crear el cliente persona. "
                            + "Filas afectadas: "
                            + filasAfectadas
            );
        }

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "SQL Server no devolvió el IDCLIENTE generado."
            );
        }

        return key.intValue();
    }

    public Integer insertClienteEmpresa(
            Integer idEmpresa
    ) {
        if (idEmpresa == null) {
            throw new IllegalArgumentException(
                    "El IDEMPRESA es obligatorio "
                            + "para crear el cliente."
            );
        }

        String sql = """
                INSERT INTO CLIENTE (
                    IDPERSONA,
                    IDEMPRESA,
                    ESTADO
                )
                VALUES (
                    NULL,
                    :idEmpresa,
                    :estado
                )
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idEmpresa",
                                idEmpresa
                        )
                        .addValue(
                                "estado",
                                EstadoUtil.ACTIVO
                        );

        KeyHolder keyHolder =
                new GeneratedKeyHolder();

        int filasAfectadas = jdbc.update(
                sql,
                params,
                keyHolder,
                new String[]{"IDCLIENTE"}
        );

        if (filasAfectadas != 1) {
            throw new IllegalStateException(
                    "No se pudo crear el cliente empresa. "
                            + "Filas afectadas: "
                            + filasAfectadas
            );
        }

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "SQL Server no devolvió el IDCLIENTE generado."
            );
        }

        return key.intValue();
    }

    public int changeEstado(
            Integer idCliente,
            String estado
    ) {
        String sql = """
                UPDATE CLIENTE
                SET ESTADO = :estado
                WHERE IDCLIENTE = :idCliente
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idCliente",
                                idCliente
                        )
                        .addValue(
                                "estado",
                                EstadoUtil.normalizar(estado)
                        );

        return jdbc.update(sql, params);
    }

    private MapSqlParameterSource filterParams(
            String texto,
            String tipo,
            String estado
    ) {
        String textoFiltro =
                texto == null || texto.isBlank()
                        ? null
                        : "%"
                        + texto.trim().toUpperCase()
                        + "%";

        String tipoFiltro =
                tipo == null || tipo.isBlank()
                        ? null
                        : tipo.trim().toUpperCase();

        return new MapSqlParameterSource()
                .addValue(
                        "texto",
                        textoFiltro
                )
                .addValue(
                        "tipo",
                        tipoFiltro
                )
                .addValue(
                        "estado",
                        EstadoUtil.normalizarFiltro(estado)
                );
    }

    private RowMapper<Cliente> clienteMapper() {
        return (rs, rowNum) -> {
            String tipo =
                    text(rs, "TIPO_CLIENTE");

            String nombres =
                    text(rs, "NOMBRES");

            String apePaterno =
                    text(rs, "APEPATERNO");

            String apeMaterno =
                    text(rs, "APEMATERNO");

            String razonSocial =
                    text(rs, "RAZON_SOCIAL");

            String dni =
                    text(rs, "DNI");

            String ruc =
                    text(rs, "RUC");

            String displayName =
                    "EMPRESA".equals(tipo)
                            ? razonSocial
                            : join(
                            nombres,
                            apePaterno,
                            apeMaterno
                    );

            String documento =
                    "EMPRESA".equals(tipo)
                            ? ruc
                            : dni;

            return new Cliente(
                    rs.getObject(
                            "IDCLIENTE",
                            Integer.class
                    ),
                    rs.getObject(
                            "IDPERSONA",
                            Integer.class
                    ),
                    rs.getObject(
                            "IDEMPRESA",
                            Integer.class
                    ),
                    text(rs, "ESTADO"),
                    tipo,
                    dni,
                    nombres,
                    apePaterno,
                    apeMaterno,
                    text(rs, "CORREO"),
                    text(rs, "CELULAR"),
                    ruc,
                    razonSocial,
                    text(rs, "TELEFONO"),
                    displayName,
                    documento
            );
        };
    }

    private static String join(String... values) {
        StringBuilder builder =
                new StringBuilder();

        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }

            if (!builder.isEmpty()) {
                builder.append(' ');
            }

            builder.append(value.trim());
        }

        return builder.toString();
    }

    private static String text(
            ResultSet rs,
            String column
    ) throws SQLException {
        String value = rs.getString(column);

        return value == null
                ? null
                : value.trim();
    }
}