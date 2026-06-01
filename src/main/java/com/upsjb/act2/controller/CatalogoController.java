package com.upsjb.act2.controller;

import com.upsjb.act2.model.Area;
import com.upsjb.act2.model.Cargo;
import com.upsjb.act2.model.Categoria;
import com.upsjb.act2.model.Contrato;
import com.upsjb.act2.model.Departamento;
import com.upsjb.act2.model.Distrito;
import com.upsjb.act2.model.Mesa;
import com.upsjb.act2.model.MetodoPago;
import com.upsjb.act2.model.Provincia;
import com.upsjb.act2.service.CatalogoService;
import com.upsjb.act2.service.UbigeoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CatalogoController {

    private final CatalogoService catalogoService;
    private final UbigeoService ubigeoService;

    public CatalogoController(CatalogoService catalogoService, UbigeoService ubigeoService) {
        this.catalogoService = catalogoService;
        this.ubigeoService = ubigeoService;
    }

    @GetMapping("/catalogos/areas")
    public List<Area> areas() {
        return catalogoService.listarAreasActivas();
    }

    @GetMapping("/catalogos/cargos")
    public List<Cargo> cargos() {
        return catalogoService.listarCargosActivos();
    }

    @GetMapping("/catalogos/categorias")
    public List<Categoria> categorias() {
        return catalogoService.listarCategoriasActivas();
    }

    @GetMapping("/catalogos/contratos")
    public List<Contrato> contratos() {
        return catalogoService.listarContratosActivos();
    }

    @GetMapping("/catalogos/mesas")
    public List<Mesa> mesas() {
        return catalogoService.listarMesasActivas();
    }

    @GetMapping("/catalogos/metodos-pago")
    public List<MetodoPago> metodosPago() {
        return catalogoService.listarMetodosPagoActivos();
    }

    @GetMapping("/catalogos/departamentos")
    public List<Departamento> departamentos() {
        return ubigeoService.listarDepartamentosActivos();
    }

    @GetMapping("/catalogos/provincias")
    public List<Provincia> provincias(@RequestParam Integer idDepartamento) {
        return ubigeoService.listarProvinciasPorDepartamento(idDepartamento);
    }

    @GetMapping("/catalogos/distritos")
    public List<Distrito> distritos(@RequestParam Integer idProvincia) {
        return ubigeoService.listarDistritosPorProvincia(idProvincia);
    }
}