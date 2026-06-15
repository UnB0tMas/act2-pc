package com.upsjb.act2.controller;

import com.upsjb.act2.model.Cliente;
import com.upsjb.act2.model.Empresa;
import com.upsjb.act2.model.Persona;
import com.upsjb.act2.service.ClienteService;
import com.upsjb.act2.service.UbigeoService;
import com.upsjb.act2.util.EstadoUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    public ClienteController(
            ClienteService clienteService,
            UbigeoService ubigeoService
    ) {
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
        model.addAttribute(
                "pageResult",
                clienteService.listar(
                        texto,
                        tipo,
                        estado,
                        page,
                        size
                )
        );

        model.addAttribute("texto", texto);
        model.addAttribute("tipo", tipo);
        model.addAttribute("estado", estado);

        return "clientes/list";
    }

    @GetMapping("/clientes/{idCliente}")
    public String detail(
            @PathVariable Integer idCliente,
            Model model
    ) {
        Cliente cliente =
                clienteService.obtenerPorId(idCliente);

        boolean clienteActivo =
                EstadoUtil.esActivo(cliente.getEstado());

        model.addAttribute("cliente", cliente);
        model.addAttribute("clienteActivo", clienteActivo);

        model.addAttribute(
                "clienteEstadoEtiqueta",
                EstadoUtil.etiqueta(cliente.getEstado())
        );

        if (cliente.getIdPersona() != null) {
            Persona persona = clienteService.obtenerPersona(
                    cliente.getIdPersona()
            );

            model.addAttribute("persona", persona);
        }

        if (cliente.getIdEmpresa() != null) {
            Empresa empresa = clienteService.obtenerEmpresa(
                    cliente.getIdEmpresa()
            );

            model.addAttribute("empresa", empresa);
        }

        return "clientes/detail";
    }

    @GetMapping("/clientes/nuevo/persona")
    public String createPersona(Model model) {
        Persona persona = new Persona();

        /*
         * Solamente asigna un valor al objeto.
         * No modifica la clase Persona ni la base de datos.
         */
        persona.setEstado(EstadoUtil.ACTIVO);

        model.addAttribute("persona", persona);
        cargarDistritos(model);

        return "clientes/form-persona";
    }

    @PostMapping("/clientes/persona")
    public String storePersona(
            @ModelAttribute("persona") Persona persona,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        /*
         * BindingResult permite detectar, por ejemplo, una fecha
         * que no se pudo convertir a LocalDate.
         */
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "error",
                    "Algunos datos no tienen el formato esperado. "
                            + "Revisa especialmente la fecha de nacimiento."
            );

            cargarDistritos(model);

            return "clientes/form-persona";
        }

        try {
            Integer idCliente =
                    clienteService.crearClientePersona(persona);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Cliente persona registrado correctamente."
            );

            /*
             * Esta respuesta produce HTTP 302 Found.
             * Es el comportamiento normal de Post/Redirect/Get.
             */
            return "redirect:/clientes/" + idCliente;

        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error", ex.getMessage());
            cargarDistritos(model);

            return "clientes/form-persona";
        }
    }

    @GetMapping("/clientes/nuevo/empresa")
    public String createEmpresa(Model model) {
        Empresa empresa = new Empresa();
        empresa.setEstado(EstadoUtil.ACTIVO);

        model.addAttribute("empresa", empresa);

        return "clientes/form-empresa";
    }

    @PostMapping("/clientes/empresa")
    public String storeEmpresa(
            @ModelAttribute("empresa") Empresa empresa,
            RedirectAttributes redirectAttributes
    ) {
        Integer idCliente =
                clienteService.crearClienteEmpresa(empresa);

        redirectAttributes.addFlashAttribute(
                "success",
                "Cliente empresa registrado correctamente."
        );

        return "redirect:/clientes/" + idCliente;
    }

    @PostMapping("/clientes/{idCliente}/estado")
    public String changeEstado(
            @PathVariable Integer idCliente,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes
    ) {
        clienteService.cambiarEstado(idCliente, estado);

        redirectAttributes.addFlashAttribute(
                "success",
                "Estado del cliente actualizado correctamente."
        );

        return "redirect:/clientes";
    }

    private void cargarDistritos(Model model) {
        model.addAttribute(
                "distritos",
                ubigeoService
                        .listarDistritosActivosConNombreCompleto()
        );
    }
}