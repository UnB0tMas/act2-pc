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
public class Empleado {

    private Integer idEmpleado;
    private Integer idPersona;
    private Integer idContrato;
    private Integer idCargo;
    private BigDecimal salario;
    private String turno;
    private String estado;

    private String dni;
    private String nombres;
    private String apePaterno;
    private String apeMaterno;
    private String correo;
    private String celular;
    private String nomCargo;
    private String nomContrato;
    private String nombreCompleto;
}