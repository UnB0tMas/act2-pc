package com.upsjb.act2.controller;

import com.upsjb.act2.service.PedidoService;
import com.upsjb.act2.util.EstadoUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/pedidos")
    public String index(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model
    ) {
        model.addAttribute("pageResult", pedidoService.listar(desde, hasta, estado, page, size));
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("estado", estado);
        model.addAttribute("estadoUtil", EstadoUtil.class);
        return "pedidos/list";
    }

    @GetMapping("/pedidos/{idPedido}")
    public String detail(@PathVariable Integer idPedido, Model model) {
        model.addAttribute("pedido", pedidoService.obtenerPorId(idPedido));
        model.addAttribute("detalles", pedidoService.obtenerDetalle(idPedido));
        model.addAttribute("estadoUtil", EstadoUtil.class);
        return "pedidos/detail";
    }
}