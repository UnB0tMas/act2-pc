Revisé el archivo unificado del proyecto Spring Boot + Thymeleaf que compartiste. El login está cerca de funcionar, pero **no está totalmente coherente con tu data real** porque tu BD usa `ESTADO = 1`, mientras varias partes del proyecto todavía comparan o envían `A/I`. Además, tus usuarios tienen `LOGEO` con coma en el dominio, por ejemplo `gmail,com`, no `gmail.com`, lo cual puede hacer fallar el login si escribes el correo normal.

## Diagnóstico directo

Sí debes corregir estos puntos:

1. **Estados**

    * Tu BD: `1 = activo`, `0 = inactivo`.
    * Tu código mezcla `A/I` y `1/0`.
    * Esto rompe filtros, catálogos activos, mesas, ubigeo, clientes, empleados y botones de activar/inactivar.

2. **Login**

    * La tabla `USUARIO` tiene:

        * `LOGEO = Ramírez_Paredes@gmail,com`
        * `CLAVE = 52142`
    * Tu login compara exacto. Si escribes `gmail.com`, falla.
    * Debe permitir ambos: `gmail,com` y `gmail.com`.

3. **Catálogos**

    * `MesaRepository` y `UbigeoRepository` comparan solo `ESTADO = 'A'`.
    * Con tu BD actual `ESTADO = 1`, no retornarán datos activos.

4. **Creación de cliente**

    * `ClienteRepository` inserta clientes con `'A'`.
    * Debe insertar `1`.

---

# Código completo actualizado

Reemplaza estos archivos.

---

## 1. `src/main/java/com/upsjb/act2/util/EstadoUtil.java`

```java
package com.upsjb.act2.util;

public final class EstadoUtil {

    public static final String ACTIVO = "1";
    public static final String INACTIVO = "0";

    private EstadoUtil() {
    }

    public static String normalizar(String estado) {
        if (estado == null || estado.isBlank()) {
            return ACTIVO;
        }

        String value = estado.trim().toUpperCase();

        return switch (value) {
            case "1", "A", "ACTIVO", "TRUE", "S", "SI", "SÍ" -> ACTIVO;
            case "0", "I", "INACTIVO", "FALSE", "N", "NO" -> INACTIVO;
            default -> value.substring(0, 1);
        };
    }

    public static String normalizarFiltro(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }

        return normalizar(estado);
    }

    public static String invertir(String estadoActual) {
        return esActivo(estadoActual) ? INACTIVO : ACTIVO;
    }

    public static boolean esActivo(String estado) {
        if (estado == null || estado.isBlank()) {
            return false;
        }

        String value = estado.trim().toUpperCase();
        return "1".equals(value) || "A".equals(value) || "ACTIVO".equals(value);
    }

    public static boolean esInactivo(String estado) {
        if (estado == null || estado.isBlank()) {
            return false;
        }

        String value = estado.trim().toUpperCase();
        return "0".equals(value) || "I".equals(value) || "INACTIVO".equals(value);
    }

    public static String etiqueta(String estado) {
        if (estado == null || estado.isBlank()) {
            return "Sin estado";
        }

        String value = estado.trim().toUpperCase();

        return switch (value) {
            case "1", "A", "ACTIVO", "TRUE" -> "Activo";
            case "0", "I", "INACTIVO", "FALSE" -> "Inactivo";
            case "N" -> "Anulado";
            case "P" -> "Pendiente";
            default -> estado.trim();
        };
    }
}
```

---

## 2. `src/main/java/com/upsjb/act2/repository/UsuarioRepository.java`

```java
package com.upsjb.act2.repository;

import com.upsjb.act2.model.Usuario;
import com.upsjb.act2.security.CastillonUserDetails;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

@Repository
public class UsuarioRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public UsuarioRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<CastillonUserDetails> findUserDetailsByLogeo(String logeo) {
        String sql = """
                SELECT
                    u.IDUSUARIO,
                    u.IDEMPLEADO,
                    u.IDTIPO_USUARIO,
                    u.LOGEO,
                    u.CLAVE,
                    u.ESTADO,
                    tu.NOMUSUARIO,
                    p.NOMBRES,
                    p.APEPATERNO,
                    p.APEMATERNO,
                    c.NOMCARGO
                FROM USUARIO u
                LEFT JOIN TIPO_USUARIO tu ON tu.IDTIPO_USUARIO = u.IDTIPO_USUARIO
                LEFT JOIN EMPLEADO e ON e.IDEMPLEADO = u.IDEMPLEADO
                LEFT JOIN PERSONA p ON p.IDPERSONA = e.IDPERSONA
                LEFT JOIN CARGO c ON c.IDCARGO = e.IDCARGO
                WHERE REPLACE(LOWER(LTRIM(RTRIM(u.LOGEO))), ',', '.')
                      COLLATE Latin1_General_CI_AI = :logeoNormalizado COLLATE Latin1_General_CI_AI
                  AND LTRIM(RTRIM(CAST(u.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                  AND (
                        tu.IDTIPO_USUARIO IS NULL
                        OR LTRIM(RTRIM(CAST(tu.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                  )
                  AND (
                        e.IDEMPLEADO IS NULL
                        OR LTRIM(RTRIM(CAST(e.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                  )
                """;

        CastillonUserDetails user = DataAccessUtils.singleResult(
                jdbc.query(
                        sql,
                        new MapSqlParameterSource("logeoNormalizado", normalizarLogeo(logeo)),
                        userDetailsMapper()
                )
        );

        return Optional.ofNullable(user);
    }

    public Optional<Usuario> findById(Integer idUsuario) {
        String sql = """
                SELECT
                    u.IDUSUARIO,
                    u.IDEMPLEADO,
                    u.IDTIPO_USUARIO,
                    u.LOGEO,
                    u.CLAVE,
                    u.ESTADO,
                    tu.NOMUSUARIO,
                    p.NOMBRES,
                    p.APEPATERNO,
                    p.APEMATERNO,
                    c.NOMCARGO
                FROM USUARIO u
                LEFT JOIN TIPO_USUARIO tu ON tu.IDTIPO_USUARIO = u.IDTIPO_USUARIO
                LEFT JOIN EMPLEADO e ON e.IDEMPLEADO = u.IDEMPLEADO
                LEFT JOIN PERSONA p ON p.IDPERSONA = e.IDPERSONA
                LEFT JOIN CARGO c ON c.IDCARGO = e.IDCARGO
                WHERE u.IDUSUARIO = :idUsuario
                """;

        Usuario usuario = DataAccessUtils.singleResult(
                jdbc.query(sql, new MapSqlParameterSource("idUsuario", idUsuario), usuarioMapper())
        );

        return Optional.ofNullable(usuario);
    }

    private static String normalizarLogeo(String logeo) {
        if (logeo == null) {
            return "";
        }

        return logeo.trim()
                .replace(',', '.')
                .toLowerCase(Locale.ROOT);
    }

    private RowMapper<CastillonUserDetails> userDetailsMapper() {
        return (rs, rowNum) -> new CastillonUserDetails(
                rs.getObject("IDUSUARIO", Integer.class),
                rs.getObject("IDEMPLEADO", Integer.class),
                rs.getObject("IDTIPO_USUARIO", Integer.class),
                text(rs, "LOGEO"),
                text(rs, "CLAVE"),
                text(rs, "ESTADO"),
                text(rs, "NOMUSUARIO"),
                text(rs, "NOMBRES"),
                text(rs, "APEPATERNO"),
                text(rs, "APEMATERNO"),
                text(rs, "NOMCARGO")
        );
    }

    private RowMapper<Usuario> usuarioMapper() {
        return (rs, rowNum) -> new Usuario(
                rs.getObject("IDUSUARIO", Integer.class),
                rs.getObject("IDEMPLEADO", Integer.class),
                rs.getObject("IDTIPO_USUARIO", Integer.class),
                text(rs, "LOGEO"),
                text(rs, "CLAVE"),
                text(rs, "ESTADO"),
                text(rs, "NOMUSUARIO"),
                text(rs, "NOMBRES"),
                text(rs, "APEPATERNO"),
                text(rs, "APEMATERNO"),
                text(rs, "NOMCARGO")
        );
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}
```

---

## 3. `src/main/java/com/upsjb/act2/repository/ClienteRepository.java`

```java
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

    public ClienteRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Cliente> findPage(String texto, String tipo, String estado, int page, int size) {
        MapSqlParameterSource params = filterParams(texto, tipo, estado);
        params.addValue("offset", Math.max(page, 0) * size);
        params.addValue("size", size);

        String sql = """
                SELECT
                    c.IDCLIENTE,
                    c.IDPERSONA,
                    c.IDEMPRESA,
                    c.ESTADO,
                    CASE
                        WHEN c.IDPERSONA IS NOT NULL THEN 'PERSONA'
                        WHEN c.IDEMPRESA IS NOT NULL THEN 'EMPRESA'
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
                LEFT JOIN PERSONA p ON p.IDPERSONA = c.IDPERSONA
                LEFT JOIN EMPRESA e ON e.IDEMPRESA = c.IDEMPRESA
                WHERE (:estado IS NULL OR LTRIM(RTRIM(CAST(c.ESTADO AS VARCHAR(10)))) = :estado)
                  AND (
                        :tipo IS NULL
                        OR (:tipo = 'PERSONA' AND c.IDPERSONA IS NOT NULL)
                        OR (:tipo = 'EMPRESA' AND c.IDEMPRESA IS NOT NULL)
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
                OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
                """;

        return jdbc.query(sql, params, clienteMapper());
    }

    public long count(String texto, String tipo, String estado) {
        String sql = """
                SELECT COUNT(*)
                FROM CLIENTE c
                LEFT JOIN PERSONA p ON p.IDPERSONA = c.IDPERSONA
                LEFT JOIN EMPRESA e ON e.IDEMPRESA = c.IDEMPRESA
                WHERE (:estado IS NULL OR LTRIM(RTRIM(CAST(c.ESTADO AS VARCHAR(10)))) = :estado)
                  AND (
                        :tipo IS NULL
                        OR (:tipo = 'PERSONA' AND c.IDPERSONA IS NOT NULL)
                        OR (:tipo = 'EMPRESA' AND c.IDEMPRESA IS NOT NULL)
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

        Long total = jdbc.queryForObject(sql, filterParams(texto, tipo, estado), Long.class);
        return total == null ? 0L : total;
    }

    public Optional<Cliente> findById(Integer idCliente) {
        String sql = """
                SELECT
                    c.IDCLIENTE,
                    c.IDPERSONA,
                    c.IDEMPRESA,
                    c.ESTADO,
                    CASE
                        WHEN c.IDPERSONA IS NOT NULL THEN 'PERSONA'
                        WHEN c.IDEMPRESA IS NOT NULL THEN 'EMPRESA'
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
                LEFT JOIN PERSONA p ON p.IDPERSONA = c.IDPERSONA
                LEFT JOIN EMPRESA e ON e.IDEMPRESA = c.IDEMPRESA
                WHERE c.IDCLIENTE = :idCliente
                """;

        Cliente cliente = DataAccessUtils.singleResult(
                jdbc.query(sql, new MapSqlParameterSource("idCliente", idCliente), clienteMapper())
        );

        return Optional.ofNullable(cliente);
    }

    public Integer insertClientePersona(Integer idPersona) {
        String sql = """
                INSERT INTO CLIENTE (IDPERSONA, IDEMPRESA, ESTADO)
                VALUES (:idPersona, NULL, :estado)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idPersona", idPersona)
                        .addValue("estado", EstadoUtil.ACTIVO),
                keyHolder,
                new String[]{"IDCLIENTE"}
        );

        Number key = keyHolder.getKey();
        return key == null ? null : key.intValue();
    }

    public Integer insertClienteEmpresa(Integer idEmpresa) {
        String sql = """
                INSERT INTO CLIENTE (IDPERSONA, IDEMPRESA, ESTADO)
                VALUES (NULL, :idEmpresa, :estado)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idEmpresa", idEmpresa)
                        .addValue("estado", EstadoUtil.ACTIVO),
                keyHolder,
                new String[]{"IDCLIENTE"}
        );

        Number key = keyHolder.getKey();
        return key == null ? null : key.intValue();
    }

    public int changeEstado(Integer idCliente, String estado) {
        String sql = """
                UPDATE CLIENTE
                SET ESTADO = :estado
                WHERE IDCLIENTE = :idCliente
                """;

        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("idCliente", idCliente)
                .addValue("estado", EstadoUtil.normalizar(estado)));
    }

    private MapSqlParameterSource filterParams(String texto, String tipo, String estado) {
        return new MapSqlParameterSource()
                .addValue("texto", texto == null || texto.isBlank() ? null : "%" + texto.trim().toUpperCase() + "%")
                .addValue("tipo", tipo == null || tipo.isBlank() ? null : tipo.trim().toUpperCase())
                .addValue("estado", EstadoUtil.normalizarFiltro(estado));
    }

    private RowMapper<Cliente> clienteMapper() {
        return (rs, rowNum) -> {
            String tipo = text(rs, "TIPO_CLIENTE");
            String nombres = text(rs, "NOMBRES");
            String apePaterno = text(rs, "APEPATERNO");
            String apeMaterno = text(rs, "APEMATERNO");
            String razonSocial = text(rs, "RAZON_SOCIAL");
            String dni = text(rs, "DNI");
            String ruc = text(rs, "RUC");

            String displayName = "EMPRESA".equals(tipo)
                    ? razonSocial
                    : join(nombres, apePaterno, apeMaterno);

            String documento = "EMPRESA".equals(tipo) ? ruc : dni;

            return new Cliente(
                    rs.getObject("IDCLIENTE", Integer.class),
                    rs.getObject("IDPERSONA", Integer.class),
                    rs.getObject("IDEMPRESA", Integer.class),
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
        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(value.trim());
            }
        }

        return builder.toString();
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}
```

---

## 4. `src/main/java/com/upsjb/act2/repository/MesaRepository.java`

```java
package com.upsjb.act2.repository;

import com.upsjb.act2.model.Mesa;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MesaRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public MesaRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Mesa> findAll() {
        String sql = """
                SELECT IDMESA, NUMPISO, NUNMESA, ESTADO
                FROM MESAS
                ORDER BY TRY_CONVERT(INT, LTRIM(RTRIM(NUMPISO))), TRY_CONVERT(INT, LTRIM(RTRIM(NUNMESA)))
                """;
        return jdbc.query(sql, mesaMapper());
    }

    public List<Mesa> findAllActivas() {
        String sql = """
                SELECT IDMESA, NUMPISO, NUNMESA, ESTADO
                FROM MESAS
                WHERE LTRIM(RTRIM(CAST(ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                ORDER BY TRY_CONVERT(INT, LTRIM(RTRIM(NUMPISO))), TRY_CONVERT(INT, LTRIM(RTRIM(NUNMESA)))
                """;
        return jdbc.query(sql, mesaMapper());
    }

    private RowMapper<Mesa> mesaMapper() {
        return (rs, rowNum) -> {
            String piso = text(rs, "NUMPISO");
            String mesa = text(rs, "NUNMESA");

            return new Mesa(
                    rs.getObject("IDMESA", Integer.class),
                    piso,
                    mesa,
                    text(rs, "ESTADO"),
                    "Piso " + safe(piso) + " - Mesa " + safe(mesa)
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
```

---

## 5. `src/main/java/com/upsjb/act2/repository/UbigeoRepository.java`

```java
package com.upsjb.act2.repository;

import com.upsjb.act2.model.Departamento;
import com.upsjb.act2.model.Distrito;
import com.upsjb.act2.model.Provincia;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UbigeoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public UbigeoRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Departamento> findDepartamentosActivos() {
        String sql = """
                SELECT IDDepartamento, NOMDEPARTAMENTO, ESTADO
                FROM DEPARTAMENTO
                WHERE LTRIM(RTRIM(CAST(ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                ORDER BY NOMDEPARTAMENTO
                """;

        return jdbc.query(sql, departamentoMapper());
    }

    public List<Provincia> findProvinciasByDepartamento(Integer idDepartamento) {
        String sql = """
                SELECT
                    p.IDPROVINCIA,
                    p.IDDEPARTAMENTO,
                    p.NOMPROVINCIA,
                    p.ESTADO,
                    d.NOMDEPARTAMENTO
                FROM PROVINCIA p
                LEFT JOIN DEPARTAMENTO d ON d.IDDepartamento = p.IDDEPARTAMENTO
                WHERE p.IDDEPARTAMENTO = :idDepartamento
                  AND LTRIM(RTRIM(CAST(p.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                ORDER BY p.NOMPROVINCIA
                """;

        return jdbc.query(sql, new MapSqlParameterSource("idDepartamento", idDepartamento), provinciaMapper());
    }

    public List<Distrito> findDistritosByProvincia(Integer idProvincia) {
        String sql = """
                SELECT
                    dis.IDDISTRITO,
                    dis.IDPROVINCIA,
                    dis.NOMDISTRITO,
                    dis.ESTADO,
                    pro.IDDEPARTAMENTO,
                    pro.NOMPROVINCIA,
                    dep.NOMDEPARTAMENTO
                FROM DISTRITO dis
                LEFT JOIN PROVINCIA pro ON pro.IDPROVINCIA = dis.IDPROVINCIA
                LEFT JOIN DEPARTAMENTO dep ON dep.IDDepartamento = pro.IDDEPARTAMENTO
                WHERE dis.IDPROVINCIA = :idProvincia
                  AND LTRIM(RTRIM(CAST(dis.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                ORDER BY dis.NOMDISTRITO
                """;

        return jdbc.query(sql, new MapSqlParameterSource("idProvincia", idProvincia), distritoMapper());
    }

    public List<Distrito> findDistritosActivosConNombreCompleto() {
        String sql = """
                SELECT
                    dis.IDDISTRITO,
                    dis.IDPROVINCIA,
                    dis.NOMDISTRITO,
                    dis.ESTADO,
                    pro.IDDEPARTAMENTO,
                    pro.NOMPROVINCIA,
                    dep.NOMDEPARTAMENTO
                FROM DISTRITO dis
                LEFT JOIN PROVINCIA pro ON pro.IDPROVINCIA = dis.IDPROVINCIA
                LEFT JOIN DEPARTAMENTO dep ON dep.IDDepartamento = pro.IDDEPARTAMENTO
                WHERE LTRIM(RTRIM(CAST(dis.ESTADO AS VARCHAR(10)))) IN ('1', 'A')
                ORDER BY dep.NOMDEPARTAMENTO, pro.NOMPROVINCIA, dis.NOMDISTRITO
                """;

        return jdbc.query(sql, distritoMapper());
    }

    private RowMapper<Departamento> departamentoMapper() {
        return (rs, rowNum) -> new Departamento(
                rs.getObject("IDDepartamento", Integer.class),
                text(rs, "NOMDEPARTAMENTO"),
                text(rs, "ESTADO")
        );
    }

    private RowMapper<Provincia> provinciaMapper() {
        return (rs, rowNum) -> new Provincia(
                rs.getObject("IDPROVINCIA", Integer.class),
                rs.getObject("IDDEPARTAMENTO", Integer.class),
                text(rs, "NOMPROVINCIA"),
                text(rs, "ESTADO"),
                text(rs, "NOMDEPARTAMENTO")
        );
    }

    private RowMapper<Distrito> distritoMapper() {
        return (rs, rowNum) -> {
            String departamento = text(rs, "NOMDEPARTAMENTO");
            String provincia = text(rs, "NOMPROVINCIA");
            String distrito = text(rs, "NOMDISTRITO");

            return new Distrito(
                    rs.getObject("IDDISTRITO", Integer.class),
                    rs.getObject("IDPROVINCIA", Integer.class),
                    distrito,
                    text(rs, "ESTADO"),
                    rs.getObject("IDDEPARTAMENTO", Integer.class),
                    provincia,
                    departamento,
                    join(departamento, provincia, distrito)
            );
        };
    }

    private static String join(String... values) {
        StringBuilder builder = new StringBuilder();

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                if (!builder.isEmpty()) {
                    builder.append(" / ");
                }
                builder.append(value.trim());
            }
        }

        return builder.toString();
    }

    private static String text(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : value.trim();
    }
}
```

---

## 6. `src/main/java/com/upsjb/act2/service/ClienteService.java`

```java
package com.upsjb.act2.service;

import com.upsjb.act2.model.Cliente;
import com.upsjb.act2.model.Empresa;
import com.upsjb.act2.model.Persona;
import com.upsjb.act2.repository.ClienteRepository;
import com.upsjb.act2.repository.EmpresaRepository;
import com.upsjb.act2.repository.PersonaRepository;
import com.upsjb.act2.util.EstadoUtil;
import com.upsjb.act2.util.PageResult;
import com.upsjb.act2.util.SqlPagination;
import com.upsjb.act2.util.TextUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final EmpresaRepository empresaRepository;

    public ClienteService(
            ClienteRepository clienteRepository,
            PersonaRepository personaRepository,
            EmpresaRepository empresaRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<Cliente> listar(String texto, String tipo, String estado, Integer page, Integer size) {
        int currentPage = SqlPagination.normalizePage(page);
        int pageSize = SqlPagination.normalizeSize(size);

        String estadoNormalizado = EstadoUtil.normalizarFiltro(estado);

        List<Cliente> clientes = clienteRepository.findPage(
                TextUtil.trimToNull(texto),
                TextUtil.trimToNull(tipo),
                estadoNormalizado,
                currentPage,
                pageSize
        );

        long total = clienteRepository.count(
                TextUtil.trimToNull(texto),
                TextUtil.trimToNull(tipo),
                estadoNormalizado
        );

        return PageResult.of(clientes, currentPage, pageSize, total);
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Integer idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + idCliente));
    }

    @Transactional(readOnly = true)
    public Persona obtenerPersona(Integer idPersona) {
        return personaRepository.findById(idPersona)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + idPersona));
    }

    @Transactional(readOnly = true)
    public Empresa obtenerEmpresa(Integer idEmpresa) {
        return empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + idEmpresa));
    }

    @Transactional
    public Integer crearClientePersona(Persona persona) {
        normalizarPersona(persona);
        validarDniNoDuplicado(persona.getDni());

        Integer idPersona = personaRepository.insert(persona);
        return clienteRepository.insertClientePersona(idPersona);
    }

    @Transactional
    public Integer crearClienteEmpresa(Empresa empresa) {
        normalizarEmpresa(empresa);
        validarRucNoDuplicado(empresa.getRuc());

        Integer idEmpresa = empresaRepository.insert(empresa);
        return clienteRepository.insertClienteEmpresa(idEmpresa);
    }

    @Transactional
    public void actualizarPersona(Persona persona) {
        normalizarPersona(persona);
        personaRepository.update(persona);
    }

    @Transactional
    public void actualizarEmpresa(Empresa empresa) {
        normalizarEmpresa(empresa);
        empresaRepository.update(empresa);
    }

    @Transactional
    public void cambiarEstado(Integer idCliente, String estado) {
        clienteRepository.changeEstado(idCliente, EstadoUtil.normalizar(estado));
    }

    private void validarDniNoDuplicado(String dni) {
        String value = TextUtil.trimToNull(dni);
        if (value != null && personaRepository.existsByDni(value)) {
            throw new IllegalArgumentException("Ya existe una persona registrada con el DNI: " + value);
        }
    }

    private void validarRucNoDuplicado(String ruc) {
        String value = TextUtil.trimToNull(ruc);
        if (value != null && empresaRepository.existsByRuc(value)) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con el RUC: " + value);
        }
    }

    private void normalizarPersona(Persona persona) {
        persona.setNombres(TextUtil.trimToNull(persona.getNombres()));
        persona.setApePaterno(TextUtil.trimToNull(persona.getApePaterno()));
        persona.setApeMaterno(TextUtil.trimToNull(persona.getApeMaterno()));
        persona.setEstCivil(TextUtil.trimToNull(persona.getEstCivil()));
        persona.setDni(TextUtil.trimToNull(persona.getDni()));
        persona.setDireccion(TextUtil.trimToNull(persona.getDireccion()));
        persona.setCelular(TextUtil.trimToNull(persona.getCelular()));
        persona.setCorreo(TextUtil.trimToNull(persona.getCorreo()));
        persona.setEstado(EstadoUtil.normalizar(persona.getEstado()));
    }

    private void normalizarEmpresa(Empresa empresa) {
        empresa.setRuc(TextUtil.trimToNull(empresa.getRuc()));
        empresa.setRazonSocial(TextUtil.trimToNull(empresa.getRazonSocial()));
        empresa.setDireccion(TextUtil.trimToNull(empresa.getDireccion()));
        empresa.setTelefono(TextUtil.trimToNull(empresa.getTelefono()));
        empresa.setEstado(EstadoUtil.normalizar(empresa.getEstado()));
    }
}
```

---

## 7. `src/main/java/com/upsjb/act2/service/EmpleadoService.java`

```java
package com.upsjb.act2.service;

import com.upsjb.act2.model.Cargo;
import com.upsjb.act2.model.Contrato;
import com.upsjb.act2.model.Empleado;
import com.upsjb.act2.model.Persona;
import com.upsjb.act2.repository.CargoRepository;
import com.upsjb.act2.repository.ContratoRepository;
import com.upsjb.act2.repository.EmpleadoRepository;
import com.upsjb.act2.repository.PersonaRepository;
import com.upsjb.act2.util.EstadoUtil;
import com.upsjb.act2.util.PageResult;
import com.upsjb.act2.util.SqlPagination;
import com.upsjb.act2.util.TextUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaRepository personaRepository;
    private final CargoRepository cargoRepository;
    private final ContratoRepository contratoRepository;

    public EmpleadoService(
            EmpleadoRepository empleadoRepository,
            PersonaRepository personaRepository,
            CargoRepository cargoRepository,
            ContratoRepository contratoRepository
    ) {
        this.empleadoRepository = empleadoRepository;
        this.personaRepository = personaRepository;
        this.cargoRepository = cargoRepository;
        this.contratoRepository = contratoRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<Empleado> listar(
            String texto,
            Integer idCargo,
            Integer idContrato,
            String estado,
            Integer page,
            Integer size
    ) {
        int currentPage = SqlPagination.normalizePage(page);
        int pageSize = SqlPagination.normalizeSize(size);
        String estadoNormalizado = EstadoUtil.normalizarFiltro(estado);

        List<Empleado> empleados = empleadoRepository.findPage(
                TextUtil.trimToNull(texto),
                idCargo,
                idContrato,
                estadoNormalizado,
                currentPage,
                pageSize
        );

        long total = empleadoRepository.count(
                TextUtil.trimToNull(texto),
                idCargo,
                idContrato,
                estadoNormalizado
        );

        return PageResult.of(empleados, currentPage, pageSize, total);
    }

    @Transactional(readOnly = true)
    public Empleado obtenerPorId(Integer idEmpleado) {
        return empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado: " + idEmpleado));
    }

    @Transactional(readOnly = true)
    public Persona obtenerPersonaDeEmpleado(Integer idEmpleado) {
        Empleado empleado = obtenerPorId(idEmpleado);

        if (empleado.getIdPersona() == null) {
            throw new IllegalArgumentException("El empleado no tiene persona asociada: " + idEmpleado);
        }

        return personaRepository.findById(empleado.getIdPersona())
                .orElseThrow(() -> new IllegalArgumentException("Persona del empleado no encontrada: " + empleado.getIdPersona()));
    }

    @Transactional(readOnly = true)
    public List<Cargo> listarCargosActivos() {
        return cargoRepository.findAllActivos();
    }

    @Transactional(readOnly = true)
    public List<Contrato> listarContratosActivos() {
        return contratoRepository.findAllActivos();
    }

    @Transactional
    public Integer crearEmpleado(Persona persona, Empleado empleado) {
        normalizarPersona(persona);
        normalizarEmpleado(empleado);

        if (persona.getDni() != null && personaRepository.existsByDni(persona.getDni())) {
            throw new IllegalArgumentException("Ya existe una persona registrada con el DNI: " + persona.getDni());
        }

        Integer idPersona = personaRepository.insert(persona);
        empleado.setIdPersona(idPersona);

        return empleadoRepository.insert(empleado);
    }

    @Transactional
    public void actualizarEmpleado(Persona persona, Empleado empleado) {
        normalizarPersona(persona);
        normalizarEmpleado(empleado);

        if (persona.getIdPersona() != null) {
            personaRepository.update(persona);
        }

        empleadoRepository.update(empleado);
    }

    @Transactional
    public void cambiarEstado(Integer idEmpleado, String estado) {
        empleadoRepository.changeEstado(idEmpleado, EstadoUtil.normalizar(estado));
    }

    private void normalizarPersona(Persona persona) {
        persona.setNombres(TextUtil.trimToNull(persona.getNombres()));
        persona.setApePaterno(TextUtil.trimToNull(persona.getApePaterno()));
        persona.setApeMaterno(TextUtil.trimToNull(persona.getApeMaterno()));
        persona.setEstCivil(TextUtil.trimToNull(persona.getEstCivil()));
        persona.setDni(TextUtil.trimToNull(persona.getDni()));
        persona.setDireccion(TextUtil.trimToNull(persona.getDireccion()));
        persona.setCelular(TextUtil.trimToNull(persona.getCelular()));
        persona.setCorreo(TextUtil.trimToNull(persona.getCorreo()));
        persona.setEstado(EstadoUtil.normalizar(persona.getEstado()));
    }

    private void normalizarEmpleado(Empleado empleado) {
        empleado.setTurno(TextUtil.trimToNull(empleado.getTurno()));
        empleado.setEstado(EstadoUtil.normalizar(empleado.getEstado()));
    }
}
```

---

## Cambios obligatorios en HTML

También cambia en tus plantillas los valores `A/I` por `1/0`.

Haz estos reemplazos globales:

```html
<option value="A" th:selected="${estado == 'A'}">Activo</option>
<option value="I" th:selected="${estado == 'I'}">Inactivo</option>
```

por:

```html
<option value="1" th:selected="${estado == '1'}">Activo</option>
<option value="0" th:selected="${estado == '0'}">Inactivo</option>
```

Y cambia este patrón:

```html
${cliente.estado == 'A' ? 'I' : 'A'}
```

por:

```html
${T(com.upsjb.act2.util.EstadoUtil).esActivo(cliente.estado) ? '0' : '1'}
```

Para empleados:

```html
${empleado.estado == 'A' ? 'I' : 'A'}
```

por:

```html
${T(com.upsjb.act2.util.EstadoUtil).esActivo(empleado.estado) ? '0' : '1'}
```

Para productos:

```html
${producto.estado == 'A' ? 'I' : 'A'}
```

por:

```html
${T(com.upsjb.act2.util.EstadoUtil).esActivo(producto.estado) ? '0' : '1'}
```

También cambia hidden inputs:

```html
value="A"
```

por:

```html
value="1"
```

---

# Credenciales para probar login

Con el código corregido, puedes entrar usando coma o punto en el correo.

| Rol           | Usuario aceptado             |   Clave |
| ------------- | ---------------------------- | ------: |
| Administrador | `Ramírez_Paredes@gmail.com`  | `52142` |
| Gerente       | `Bravo_Orellana@gmail.com`   | `43223` |
| Operario      | `Martínez_Soto@gmail.com`    | `24325` |
| Cajero        | `Yáñez_Aguirre@gmail.com`    | `58467` |
| Cajero        | `Jiménez_Zambrano@gmail.com` | `86651` |

También deberían funcionar con coma, tal como está en tu tabla:

```text
Ramírez_Paredes@gmail,com
52142
```

```text
Martínez_Soto@gmail,com
24325
```

## Conclusión

El login queda correcto si reemplazas `UsuarioRepository`.
La coherencia con la BD queda estable si usas `1/0` como estado canónico.
Los catálogos de mesas y ubigeo estaban mal porque buscaban solo `A`; con el código actualizado ya leen correctamente los registros activos `1`.
