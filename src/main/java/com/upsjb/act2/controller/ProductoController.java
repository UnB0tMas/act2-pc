package com.upsjb.act2.controller;

import com.upsjb.act2.model.Producto;
import com.upsjb.act2.service.ProductoService;
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
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/productos")
    public String index(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model
    ) {
        model.addAttribute("pageResult", productoService.listar(texto, idCategoria, estado, page, size));
        model.addAttribute("categorias", productoService.listarCategoriasActivas());
        model.addAttribute("texto", texto);
        model.addAttribute("idCategoria", idCategoria);
        model.addAttribute("estado", estado);
        model.addAttribute("estadoUtil", EstadoUtil.class);
        return "productos/list";
    }

    @GetMapping("/productos/{idProducto}")
    public String detail(@PathVariable Integer idProducto, Model model) {
        model.addAttribute("producto", productoService.obtenerPorId(idProducto));
        model.addAttribute("estadoUtil", EstadoUtil.class);
        return "productos/detail";
    }

    @GetMapping("/productos/nuevo")
    public String create(Model model) {
        Producto producto = new Producto();
        producto.setEstado(EstadoUtil.ACTIVO);

        model.addAttribute("producto", producto);
        model.addAttribute("categorias", productoService.listarCategoriasActivas());
        return "productos/form";
    }

    @PostMapping("/productos")
    public String store(@ModelAttribute Producto producto, RedirectAttributes redirectAttributes) {
        Integer idProducto = productoService.crear(producto);
        redirectAttributes.addFlashAttribute("success", "Producto registrado correctamente.");
        return "redirect:/productos/" + idProducto;
    }

    @GetMapping("/productos/{idProducto}/editar")
    public String edit(@PathVariable Integer idProducto, Model model) {
        model.addAttribute("producto", productoService.obtenerPorId(idProducto));
        model.addAttribute("categorias", productoService.listarCategoriasActivas());
        return "productos/form";
    }

    @PostMapping("/productos/{idProducto}")
    public String update(
            @PathVariable Integer idProducto,
            @ModelAttribute Producto producto,
            RedirectAttributes redirectAttributes
    ) {
        producto.setIdProducto(idProducto);
        productoService.actualizar(producto);
        redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente.");
        return "redirect:/productos/" + idProducto;
    }

    @PostMapping("/productos/{idProducto}/estado")
    public String changeEstado(
            @PathVariable Integer idProducto,
            RedirectAttributes redirectAttributes
    ) {
        productoService.alternarEstado(idProducto);
        redirectAttributes.addFlashAttribute("success", "Estado del producto actualizado correctamente.");
        return "redirect:/productos";
    }
}