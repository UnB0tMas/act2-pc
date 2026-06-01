package com.upsjb.act2.service;

import com.upsjb.act2.model.Area;
import com.upsjb.act2.model.Cargo;
import com.upsjb.act2.model.Categoria;
import com.upsjb.act2.model.Contrato;
import com.upsjb.act2.model.Mesa;
import com.upsjb.act2.model.MetodoPago;
import com.upsjb.act2.repository.AreaRepository;
import com.upsjb.act2.repository.CargoRepository;
import com.upsjb.act2.repository.CategoriaRepository;
import com.upsjb.act2.repository.ContratoRepository;
import com.upsjb.act2.repository.MesaRepository;
import com.upsjb.act2.repository.MetodoPagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogoService {

    private final AreaRepository areaRepository;
    private final CargoRepository cargoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ContratoRepository contratoRepository;
    private final MesaRepository mesaRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    public CatalogoService(
            AreaRepository areaRepository,
            CargoRepository cargoRepository,
            CategoriaRepository categoriaRepository,
            ContratoRepository contratoRepository,
            MesaRepository mesaRepository,
            MetodoPagoRepository metodoPagoRepository
    ) {
        this.areaRepository = areaRepository;
        this.cargoRepository = cargoRepository;
        this.categoriaRepository = categoriaRepository;
        this.contratoRepository = contratoRepository;
        this.mesaRepository = mesaRepository;
        this.metodoPagoRepository = metodoPagoRepository;
    }

    @Transactional(readOnly = true)
    public List<Area> listarAreasActivas() {
        return areaRepository.findAllActivas();
    }

    @Transactional(readOnly = true)
    public List<Cargo> listarCargosActivos() {
        return cargoRepository.findAllActivos();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategoriasActivas() {
        return categoriaRepository.findAllActivas();
    }

    @Transactional(readOnly = true)
    public List<Contrato> listarContratosActivos() {
        return contratoRepository.findAllActivos();
    }

    @Transactional(readOnly = true)
    public List<Mesa> listarMesasActivas() {
        return mesaRepository.findAllActivas();
    }

    @Transactional(readOnly = true)
    public List<MetodoPago> listarMetodosPagoActivos() {
        return metodoPagoRepository.findAllActivos();
    }
}