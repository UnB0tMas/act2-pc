package com.upsjb.act2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Distrito {

    private Integer idDistrito;
    private Integer idProvincia;
    private String nomDistrito;
    private String estado;

    private Integer idDepartamento;
    private String nomProvincia;
    private String nomDepartamento;
    private String nombreCompleto;
}