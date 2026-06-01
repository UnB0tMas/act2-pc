package com.upsjb.act2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResumen {

    private long totalProductosActivos;
    private long totalClientesActivos;
    private long totalEmpleadosActivos;
    private long totalVentas;
    private long totalPedidos;
    private long totalMesas;

    private List<Venta> ultimasVentas;
    private List<Pedido> ultimosPedidos;
}