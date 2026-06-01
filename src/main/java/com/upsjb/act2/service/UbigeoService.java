package com.upsjb.act2.service;

import com.upsjb.act2.model.Departamento;
import com.upsjb.act2.model.Distrito;
import com.upsjb.act2.model.Provincia;
import com.upsjb.act2.repository.UbigeoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UbigeoService {

    private final UbigeoRepository ubigeoRepository;

    public UbigeoService(UbigeoRepository ubigeoRepository) {
        this.ubigeoRepository = ubigeoRepository;
    }

    @Transactional(readOnly = true)
    public List<Departamento> listarDepartamentosActivos() {
        return ubigeoRepository.findDepartamentosActivos();
    }

    @Transactional(readOnly = true)
    public List<Provincia> listarProvinciasPorDepartamento(Integer idDepartamento) {
        if (idDepartamento == null) {
            return List.of();
        }
        return ubigeoRepository.findProvinciasByDepartamento(idDepartamento);
    }

    @Transactional(readOnly = true)
    public List<Distrito> listarDistritosPorProvincia(Integer idProvincia) {
        if (idProvincia == null) {
            return List.of();
        }
        return ubigeoRepository.findDistritosByProvincia(idProvincia);
    }

    @Transactional(readOnly = true)
    public List<Distrito> listarDistritosActivosConNombreCompleto() {
        return ubigeoRepository.findDistritosActivosConNombreCompleto();
    }
}