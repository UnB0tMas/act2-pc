package com.upsjb.act2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    private Integer idVentas;
    private Integer idCliente;
    private Integer idMetodoPago;
    private Integer idPedido;
    private Integer idUsuario;
    private LocalDate fechaVenta;
    private String documento;
    private BigDecimal montoTotal;
    private BigDecimal descuento;
    private BigDecimal subTotal;
    private BigDecimal igv;
    private BigDecimal totalPagar;
    private String estado;

    private String clienteNombre;
    private String clienteDocumento;
    private String metodoPago;
    private String logeoUsuario;
}