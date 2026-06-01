package com.upsjb.act2.controller;

import com.upsjb.act2.service.VentaService;
import com.upsjb.act2.util.EstadoUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping("/ventas")
    public String index(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer idMetodoPago,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model
    ) {
        model.addAttribute("pageResult", ventaService.listar(desde, hasta, idMetodoPago, estado, page, size));
        model.addAttribute("metodosPago", ventaService.listarMetodosPagoActivos());
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("idMetodoPago", idMetodoPago);
        model.addAttribute("estado", estado);
        model.addAttribute("estadoUtil", EstadoUtil.class);
        return "ventas/list";
    }

    @GetMapping("/ventas/{idVentas}")
    public String detail(@PathVariable Integer idVentas, Model model) {
        model.addAttribute("venta", ventaService.obtenerPorId(idVentas));
        model.addAttribute("detalles", ventaService.obtenerDetalle(idVentas));
        model.addAttribute("estadoUtil", EstadoUtil.class);
        return "ventas/detail";
    }
}