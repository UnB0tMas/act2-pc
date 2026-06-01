package com.upsjb.act2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    private Integer idEmpresa;
    private String ruc;
    private String razonSocial;
    private String direccion;
    private String telefono;
    private String estado;
}