package com.upsjb.act2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Persona {

    private Integer idPersona;
    private Integer idDistrito;
    private String nombres;
    private String apePaterno;
    private String apeMaterno;
    private String estCivil;
    private String dni;
    private String direccion;
    private String celular;
    private LocalDate fecNac;
    private String correo;
    private String estado;

    private String nomDistrito;
    private String nomProvincia;
    private String nomDepartamento;
    private String nombreCompleto;
}