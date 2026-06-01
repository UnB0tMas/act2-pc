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