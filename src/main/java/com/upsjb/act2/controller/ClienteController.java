package com.upsjb.act2.controller;

import com.upsjb.act2.model.Cliente;
import com.upsjb.act2.model.Empresa;
import com.upsjb.act2.model.Persona;
import com.upsjb.act2.service.ClienteService;
import com.upsjb.act2.service.UbigeoService;
import com.upsjb.act2.util.EstadoUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ClienteController {

    private final ClienteService clienteService;
    private final UbigeoService ubigeoService;

    public ClienteController(ClienteService clienteService, UbigeoService ubigeoService) {
        this.clienteService = clienteService;
        this.ubigeoService = ubigeoService;
    }

    @GetMapping("/clientes")
    public String index(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model
    ) {
        model.addAttribute("pageResult", clienteService.listar(texto, tipo, estado, page, size));
        model.addAttribute("texto", texto);
        model.addAttribute("tipo", tipo);
        model.addAttribute("estado", estado);
        model.addAttribute("estadoUtil", EstadoUtil.class);
        return "clientes/list";
    }

    @GetMapping("/clientes/{idCliente}")
    public String detail(@PathVariable Integer idCliente, Model model) {
        Cliente cliente = clienteService.obtenerPorId(idCliente);

        model.addAttribute("cliente", cliente);
        model.addAttribute("estadoUtil", EstadoUtil.class);

        if (cliente.getIdPersona() != null) {
            model.addAttribute("persona", clienteService.obtenerPersona(cliente.getIdPersona()));
        }

        if (cliente.getIdEmpresa() != null) {
            model.addAttribute("empresa", clienteService.obtenerEmpresa(cliente.getIdEmpresa()));
        }

        return "clientes/detail";
    }

    @GetMapping("/clientes/nuevo/persona")
    public String createPersona(Model model) {
        model.addAttribute("persona", new Persona());
        model.addAttribute("distritos", ubigeoService.listarDistritosActivosConNombreCompleto());
        return "clientes/form-persona";
    }

    @PostMapping("/clientes/persona")
    public String storePersona(@ModelAttribute Persona persona, RedirectAttributes redirectAttributes) {
        Integer idCliente = clienteService.crearClientePersona(persona);
        redirectAttributes.addFlashAttribute("success", "Cliente persona registrado correctamente.");
        return "redirect:/clientes/" + idCliente;
    }

    @GetMapping("/clientes/nuevo/empresa")
    public String createEmpresa(Model model) {
        model.addAttribute("empresa", new Empresa());
        return "clientes/form-empresa";
    }

    @PostMapping("/clientes/empresa")
    public String storeEmpresa(@ModelAttribute Empresa empresa, RedirectAttributes redirectAttributes) {
        Integer idCliente = clienteService.crearClienteEmpresa(empresa);
        redirectAttributes.addFlashAttribute("success", "Cliente empresa registrado correctamente.");
        return "redirect:/clientes/" + idCliente;
    }

    @PostMapping("/clientes/{idCliente}/estado")
    public String changeEstado(
            @PathVariable Integer idCliente,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes
    ) {
        clienteService.cambiarEstado(idCliente, estado);
        redirectAttributes.addFlashAttribute("success", "Estado del cliente actualizado correctamente.");
        return "redirect:/clientes";
    }
}