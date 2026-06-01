package com.upsjb.act2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedido {

    private Integer idDetallePedido;
    private Integer idPedido;
    private Integer idProducto;
    private Integer idArea;
    private Integer idUsuario;
    private Integer cantidad;
    private BigDecimal precio;
    private String estado;

    private String nomProducto;
    private String nomArea;
    private String logeoUsuario;
    private BigDecimal subtotal;
}