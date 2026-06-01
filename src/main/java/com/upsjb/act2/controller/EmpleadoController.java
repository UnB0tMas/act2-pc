package com.upsjb.act2.controller;

import com.upsjb.act2.model.Empleado;
import com.upsjb.act2.model.Persona;
import com.upsjb.act2.service.EmpleadoService;
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
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final UbigeoService ubigeoService;

    public EmpleadoController(EmpleadoService empleadoService, UbigeoService ubigeoService) {
        this.empleadoService = empleadoService;
        this.ubigeoService = ubigeoService;
    }

    @GetMapping("/empleados")
    public String index(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Integer idCargo,
            @RequestParam(required = false) Integer idContrato,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model
    ) {
        model.addAttribute("pageResult", empleadoService.listar(texto, idCargo, idContrato, estado, page, size));
        model.addAttribute("cargos", empleadoService.listarCargosActivos());
        model.addAttribute("contratos", empleadoService.listarContratosActivos());
        model.addAttribute("texto", texto);
        model.addAttribute("idCargo", idCargo);
        model.addAttribute("idContrato", idContrato);
        model.addAttribute("estado", estado);
        model.addAttribute("estadoUtil", EstadoUtil.class);
        return "empleados/list";
    }

    @GetMapping("/empleados/{idEmpleado}")
    public String detail(@PathVariable Integer idEmpleado, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(idEmpleado));
        model.addAttribute("persona", empleadoService.obtenerPersonaDeEmpleado(idEmpleado));
        model.addAttribute("estadoUtil", EstadoUtil.class);
        return "empleados/detail";
    }

    @GetMapping("/empleados/nuevo")
    public String create(Model model) {
        model.addAttribute("persona", new Persona());
        model.addAttribute("empleado", new Empleado());
        addEmpleadoCombos(model);
        return "empleados/form";
    }

    @PostMapping("/empleados")
    public String store(
            @ModelAttribute Persona persona,
            @ModelAttribute Empleado empleado,
            RedirectAttributes redirectAttributes
    ) {
        Integer idEmpleado = empleadoService.crearEmpleado(persona, empleado);
        redirectAttributes.addFlashAttribute("success", "Empleado registrado correctamente.");
        return "redirect:/empleados/" + idEmpleado;
    }

    @GetMapping("/empleados/{idEmpleado}/editar")
    public String edit(@PathVariable Integer idEmpleado, Model model) {
        model.addAttribute("empleado", empleadoService.obtenerPorId(idEmpleado));
        model.addAttribute("persona", empleadoService.obtenerPersonaDeEmpleado(idEmpleado));
        addEmpleadoCombos(model);
        return "empleados/form";
    }

    @PostMapping("/empleados/{idEmpleado}")
    public String update(
            @PathVariable Integer idEmpleado,
            @ModelAttribute Persona persona,
            @ModelAttribute Empleado empleado,
            RedirectAttributes redirectAttributes
    ) {
        empleado.setIdEmpleado(idEmpleado);
        empleadoService.actualizarEmpleado(persona, empleado);
        redirectAttributes.addFlashAttribute("success", "Empleado actualizado correctamente.");
        return "redirect:/empleados/" + idEmpleado;
    }

    @PostMapping("/empleados/{idEmpleado}/estado")
    public String changeEstado(
            @PathVariable Integer idEmpleado,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes
    ) {
        empleadoService.cambiarEstado(idEmpleado, estado);
        redirectAttributes.addFlashAttribute("success", "Estado del empleado actualizado correctamente.");
        return "redirect:/empleados";
    }

    private void addEmpleadoCombos(Model model) {
        model.addAttribute("cargos", empleadoService.listarCargosActivos());
        model.addAttribute("contratos", empleadoService.listarContratosActivos());
        model.addAttribute("distritos", ubigeoService.listarDistritosActivosConNombreCompleto());
    }
}