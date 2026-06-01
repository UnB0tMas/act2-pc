package com.upsjb.act2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Integer idUsuario;
    private Integer idEmpleado;
    private Integer idTipoUsuario;
    private String logeo;
    private String clave;
    private String estado;

    private String nomUsuario;
    private String nombres;
    private String apePaterno;
    private String apeMaterno;
    private String nomCargo;
}