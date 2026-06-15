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

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ClienteService {

    private static final Pattern DNI_PATTERN =
            Pattern.compile("^\\d{8}$");

    private static final Pattern CELULAR_PATTERN =
            Pattern.compile("^\\d{9}$");

    private static final Pattern CORREO_PATTERN =
            Pattern.compile(
                    "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
                    Pattern.CASE_INSENSITIVE
            );

    private static final Set<String> ESTADOS_CIVILES =
            Set.of("SO", "CA", "VI", "DI");

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
    public PageResult<Cliente> listar(
            String texto,
            String tipo,
            String estado,
            Integer page,
            Integer size
    ) {
        int currentPage =
                SqlPagination.normalizePage(page);

        int pageSize =
                SqlPagination.normalizeSize(size);

        String estadoNormalizado =
                EstadoUtil.normalizarFiltro(estado);

        List<Cliente> clientes =
                clienteRepository.findPage(
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

        return PageResult.of(
                clientes,
                currentPage,
                pageSize,
                total
        );
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Integer idCliente) {
        if (idCliente == null) {
            throw new IllegalArgumentException(
                    "El ID del cliente es obligatorio."
            );
        }

        return clienteRepository.findById(idCliente)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cliente no encontrado: "
                                        + idCliente
                        )
                );
    }

    @Transactional(readOnly = true)
    public Persona obtenerPersona(Integer idPersona) {
        if (idPersona == null) {
            throw new IllegalArgumentException(
                    "El ID de la persona es obligatorio."
            );
        }

        return personaRepository.findById(idPersona)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Persona no encontrada: "
                                        + idPersona
                        )
                );
    }

    @Transactional(readOnly = true)
    public Empresa obtenerEmpresa(Integer idEmpresa) {
        if (idEmpresa == null) {
            throw new IllegalArgumentException(
                    "El ID de la empresa es obligatorio."
            );
        }

        return empresaRepository.findById(idEmpresa)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Empresa no encontrada: "
                                        + idEmpresa
                        )
                );
    }

    /**
     * Crea primero la persona y después el cliente.
     *
     * Al estar dentro de @Transactional:
     * - si falla PERSONA, no se crea CLIENTE;
     * - si falla CLIENTE, se revierte la inserción de PERSONA.
     */
    @Transactional
    public Integer crearClientePersona(Persona persona) {
        if (persona == null) {
            throw new IllegalArgumentException(
                    "Los datos de la persona son obligatorios."
            );
        }

        normalizarPersona(persona);
        validarPersonaParaCrear(persona);
        validarDniNoDuplicado(persona.getDni());

        Integer idPersona =
                personaRepository.insert(persona);

        if (idPersona == null) {
            throw new IllegalStateException(
                    "La persona fue insertada, pero no se obtuvo "
                            + "el IDPERSONA generado."
            );
        }

        persona.setIdPersona(idPersona);

        Integer idCliente =
                clienteRepository.insertClientePersona(
                        idPersona,
                        persona.getEstado()
                );

        if (idCliente == null) {
            throw new IllegalStateException(
                    "El cliente fue insertado, pero no se obtuvo "
                            + "el IDCLIENTE generado."
            );
        }

        return idCliente;
    }

    @Transactional
    public Integer crearClienteEmpresa(Empresa empresa) {
        if (empresa == null) {
            throw new IllegalArgumentException(
                    "Los datos de la empresa son obligatorios."
            );
        }

        normalizarEmpresa(empresa);
        validarRucNoDuplicado(empresa.getRuc());

        Integer idEmpresa =
                empresaRepository.insert(empresa);

        if (idEmpresa == null) {
            throw new IllegalStateException(
                    "No se obtuvo el ID de la empresa registrada."
            );
        }

        Integer idCliente =
                clienteRepository.insertClienteEmpresa(idEmpresa);

        if (idCliente == null) {
            throw new IllegalStateException(
                    "No se obtuvo el ID del cliente empresa."
            );
        }

        return idCliente;
    }

    @Transactional
    public void actualizarPersona(Persona persona) {
        if (persona == null || persona.getIdPersona() == null) {
            throw new IllegalArgumentException(
                    "La persona que se desea actualizar no es válida."
            );
        }

        normalizarPersona(persona);

        int filasAfectadas =
                personaRepository.update(persona);

        if (filasAfectadas != 1) {
            throw new IllegalArgumentException(
                    "No se encontró la persona que se desea actualizar."
            );
        }
    }

    @Transactional
    public void actualizarEmpresa(Empresa empresa) {
        if (empresa == null || empresa.getIdEmpresa() == null) {
            throw new IllegalArgumentException(
                    "La empresa que se desea actualizar no es válida."
            );
        }

        normalizarEmpresa(empresa);

        int filasAfectadas =
                empresaRepository.update(empresa);

        if (filasAfectadas != 1) {
            throw new IllegalArgumentException(
                    "No se encontró la empresa que se desea actualizar."
            );
        }
    }

    @Transactional
    public void cambiarEstado(
            Integer idCliente,
            String estado
    ) {
        if (idCliente == null) {
            throw new IllegalArgumentException(
                    "El ID del cliente es obligatorio."
            );
        }

        int filasAfectadas =
                clienteRepository.changeEstado(
                        idCliente,
                        EstadoUtil.normalizar(estado)
                );

        if (filasAfectadas != 1) {
            throw new IllegalArgumentException(
                    "No se encontró el cliente: "
                            + idCliente
            );
        }
    }

    private void validarPersonaParaCrear(Persona persona) {
        requireText(
                persona.getDni(),
                "El DNI es obligatorio."
        );

        requireText(
                persona.getNombres(),
                "Los nombres son obligatorios."
        );

        requireText(
                persona.getApePaterno(),
                "El apellido paterno es obligatorio."
        );

        requireText(
                persona.getApeMaterno(),
                "El apellido materno es obligatorio."
        );

        requireText(
                persona.getCelular(),
                "El celular es obligatorio."
        );

        requireText(
                persona.getCorreo(),
                "El correo es obligatorio."
        );

        requireText(
                persona.getEstCivil(),
                "El estado civil es obligatorio."
        );

        requireText(
                persona.getDireccion(),
                "La dirección es obligatoria."
        );

        if (persona.getIdDistrito() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un distrito."
            );
        }

        if (persona.getFecNac() == null) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento es obligatoria."
            );
        }

        if (!DNI_PATTERN.matcher(persona.getDni()).matches()) {
            throw new IllegalArgumentException(
                    "El DNI debe contener exactamente 8 dígitos."
            );
        }

        if (!CELULAR_PATTERN
                .matcher(persona.getCelular())
                .matches()) {

            throw new IllegalArgumentException(
                    "El celular debe contener exactamente 9 dígitos."
            );
        }

        if (!CORREO_PATTERN
                .matcher(persona.getCorreo())
                .matches()) {

            throw new IllegalArgumentException(
                    "El correo electrónico no tiene un formato válido."
            );
        }

        if (!ESTADOS_CIVILES.contains(
                persona.getEstCivil()
        )) {
            throw new IllegalArgumentException(
                    "El estado civil seleccionado no es válido."
            );
        }

        if (persona.getFecNac().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento no puede ser futura."
            );
        }
    }

    private void validarDniNoDuplicado(String dni) {
        String value = TextUtil.trimToNull(dni);

        if (value != null
                && personaRepository.existsByDni(value)) {

            throw new IllegalArgumentException(
                    "Ya existe una persona registrada con el DNI: "
                            + value
            );
        }
    }

    private void validarRucNoDuplicado(String ruc) {
        String value = TextUtil.trimToNull(ruc);

        if (value != null
                && empresaRepository.existsByRuc(value)) {

            throw new IllegalArgumentException(
                    "Ya existe una empresa registrada con el RUC: "
                            + value
            );
        }
    }

    private void normalizarPersona(Persona persona) {
        persona.setNombres(
                TextUtil.trimToNull(persona.getNombres())
        );

        persona.setApePaterno(
                TextUtil.trimToNull(persona.getApePaterno())
        );

        persona.setApeMaterno(
                TextUtil.trimToNull(persona.getApeMaterno())
        );

        persona.setDni(
                TextUtil.trimToNull(persona.getDni())
        );

        persona.setDireccion(
                TextUtil.trimToNull(persona.getDireccion())
        );

        persona.setCelular(
                TextUtil.trimToNull(persona.getCelular())
        );

        String correo =
                TextUtil.trimToNull(persona.getCorreo());

        persona.setCorreo(
                correo == null
                        ? null
                        : correo.toLowerCase(Locale.ROOT)
        );

        String estadoCivil =
                TextUtil.trimToNull(persona.getEstCivil());

        persona.setEstCivil(
                estadoCivil == null
                        ? null
                        : estadoCivil.toUpperCase(Locale.ROOT)
        );

        persona.setEstado(
                EstadoUtil.normalizar(persona.getEstado())
        );
    }

    private void normalizarEmpresa(Empresa empresa) {
        empresa.setRuc(
                TextUtil.trimToNull(empresa.getRuc())
        );

        empresa.setRazonSocial(
                TextUtil.trimToNull(empresa.getRazonSocial())
        );

        empresa.setDireccion(
                TextUtil.trimToNull(empresa.getDireccion())
        );

        empresa.setTelefono(
                TextUtil.trimToNull(empresa.getTelefono())
        );

        empresa.setEstado(
                EstadoUtil.normalizar(empresa.getEstado())
        );
    }

    private static void requireText(
            String value,
            String message
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}