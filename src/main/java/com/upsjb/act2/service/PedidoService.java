package com.upsjb.act2.service;

import com.upsjb.act2.model.DetallePedido;
import com.upsjb.act2.model.Pedido;
import com.upsjb.act2.repository.PedidoRepository;
import com.upsjb.act2.util.PageResult;
import com.upsjb.act2.util.SqlPagination;
import com.upsjb.act2.util.TextUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<Pedido> listar(LocalDate desde, LocalDate hasta, String estado, Integer page, Integer size) {
        int currentPage = SqlPagination.normalizePage(page);
        int pageSize = SqlPagination.normalizeSize(size);

        List<Pedido> pedidos = pedidoRepository.findPage(
                desde,
                hasta,
                TextUtil.trimToNull(estado),
                currentPage,
                pageSize
        );

        long total = pedidoRepository.count(desde, hasta, TextUtil.trimToNull(estado));

        return PageResult.of(pedidos, currentPage, pageSize, total);
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPorId(Integer idPedido) {
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + idPedido));
    }

    @Transactional(readOnly = true)
    public List<DetallePedido> obtenerDetalle(Integer idPedido) {
        return pedidoRepository.findDetalleByPedido(idPedido);
    }
}