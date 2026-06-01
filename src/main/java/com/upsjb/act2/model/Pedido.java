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
public class Pedido {

    private Integer idPedido;
    private Integer idMesa;
    private LocalDate fecha;
    private String estado;

    private String numPiso;
    private String nunMesa;
    private String mesaDescripcion;
    private Integer totalItems;
    private BigDecimal total;
}