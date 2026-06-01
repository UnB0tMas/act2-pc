package com.upsjb.act2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mesa {

    private Integer idMesa;
    private String numPiso;
    private String nunMesa;
    private String estado;
    private String descripcion;
}