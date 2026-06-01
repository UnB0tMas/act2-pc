package com.upsjb.act2.controller;

import com.upsjb.act2.service.AuthService;
import com.upsjb.act2.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthService authService;

    public DashboardController(DashboardService dashboardService, AuthService authService) {
        this.dashboardService = dashboardService;
        this.authService = authService;
    }

    @GetMapping("/dashboard")
    public String index(Model model) {
        model.addAttribute("resumen", dashboardService.obtenerResumen());
        model.addAttribute("usuarioNombre", authService.obtenerNombreVisible());
        model.addAttribute("usuarioTipo", authService.obtenerTipoUsuario());
        return "dashboard/index";
    }
}