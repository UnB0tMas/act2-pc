package com.upsjb.act2.service;

import com.upsjb.act2.model.DetallePedido;
import com.upsjb.act2.model.MetodoPago;
import com.upsjb.act2.model.Venta;
import com.upsjb.act2.repository.MetodoPagoRepository;
import com.upsjb.act2.repository.VentaRepository;
import com.upsjb.act2.util.PageResult;
import com.upsjb.act2.util.SqlPagination;
import com.upsjb.act2.util.TextUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    public VentaService(VentaRepository ventaRepository, MetodoPagoRepository metodoPagoRepository) {
        this.ventaRepository = ventaRepository;
        this.metodoPagoRepository = metodoPagoRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<Venta> listar(
            LocalDate desde,
            LocalDate hasta,
            Integer idMetodoPago,
            String estado,
            Integer page,
            Integer size
    ) {
        int currentPage = SqlPagination.normalizePage(page);
        int pageSize = SqlPagination.normalizeSize(size);

        List<Venta> ventas = ventaRepository.findPage(
                desde,
                hasta,
                idMetodoPago,
                TextUtil.trimToNull(estado),
                currentPage,
                pageSize
        );

        long total = ventaRepository.count(desde, hasta, idMetodoPago, TextUtil.trimToNull(estado));

        return PageResult.of(ventas, currentPage, pageSize, total);
    }

    @Transactional(readOnly = true)
    public Venta obtenerPorId(Integer idVentas) {
        return ventaRepository.findById(idVentas)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + idVentas));
    }

    @Transactional(readOnly = true)
    public List<DetallePedido> obtenerDetalle(Integer idVentas) {
        return ventaRepository.findDetalleByVenta(idVentas);
    }

    @Transactional(readOnly = true)
    public List<MetodoPago> listarMetodosPagoActivos() {
        return metodoPagoRepository.findAllActivos();
    }
}