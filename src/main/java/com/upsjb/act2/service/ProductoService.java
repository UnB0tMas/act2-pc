package com.upsjb.act2.service;

import com.upsjb.act2.model.Categoria;
import com.upsjb.act2.model.Producto;
import com.upsjb.act2.repository.CategoriaRepository;
import com.upsjb.act2.repository.ProductoRepository;
import com.upsjb.act2.util.EstadoUtil;
import com.upsjb.act2.util.PageResult;
import com.upsjb.act2.util.SqlPagination;
import com.upsjb.act2.util.TextUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<Producto> listar(String texto, Integer idCategoria, String estado, Integer page, Integer size) {
        int currentPage = SqlPagination.normalizePage(page);
        int pageSize = SqlPagination.normalizeSize(size);

        List<Producto> productos = productoRepository.findPage(
                TextUtil.trimToNull(texto),
                idCategoria,
                EstadoUtil.normalizarFiltro(estado),
                currentPage,
                pageSize
        );

        long total = productoRepository.count(
                TextUtil.trimToNull(texto),
                idCategoria,
                EstadoUtil.normalizarFiltro(estado)
        );

        return PageResult.of(productos, currentPage, pageSize, total);
    }

    @Transactional(readOnly = true)
    public Producto obtenerPorId(Integer idProducto) {
        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + idProducto));
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategoriasActivas() {
        return categoriaRepository.findAllActivas();
    }

    @Transactional
    public Integer crear(Producto producto) {
        producto.setEstado(EstadoUtil.normalizar(producto.getEstado()));
        normalizarProducto(producto);
        return productoRepository.insert(producto);
    }

    @Transactional
    public void actualizar(Producto producto) {
        producto.setEstado(EstadoUtil.normalizar(producto.getEstado()));
        normalizarProducto(producto);
        productoRepository.update(producto);
    }

    @Transactional
    public void cambiarEstado(Integer idProducto, String estado) {
        productoRepository.changeEstado(idProducto, EstadoUtil.normalizar(estado));
    }

    @Transactional
    public void alternarEstado(Integer idProducto) {
        Producto producto = obtenerPorId(idProducto);
        productoRepository.changeEstado(idProducto, EstadoUtil.invertir(producto.getEstado()));
    }

    private void normalizarProducto(Producto producto) {
        producto.setNomProducto(TextUtil.trimToNull(producto.getNomProducto()));
        producto.setDescripcion(TextUtil.trimToNull(producto.getDescripcion()));
        producto.setMarca(TextUtil.trimToNull(producto.getMarca()));
    }
}