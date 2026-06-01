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