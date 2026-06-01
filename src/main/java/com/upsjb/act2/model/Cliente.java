package com.upsjb.act2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    private Integer idCliente;
    private Integer idPersona;
    private Integer idEmpresa;
    private String estado;

    private String tipoCliente;

    private String dni;
    private String nombres;
    private String apePaterno;
    private String apeMaterno;
    private String correo;
    private String celular;

    private String ruc;
    private String razonSocial;
    private String telefonoEmpresa;

    private String displayName;
    private String documento;
}