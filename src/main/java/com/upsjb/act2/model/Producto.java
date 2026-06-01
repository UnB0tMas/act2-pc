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
public class Producto {

    private Integer idProducto;
    private Integer idCategoria;
    private String nomCategoria;
    private String nomProducto;
    private String descripcion;
    private BigDecimal precio;
    private String marca;
    private String estado;
}