package com.academit.education.dto.materia;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Response de materia según el contrato (MateriaDTO del openapi).
 * anio es el año de cursada dentro del plan (anio_plan de plan_materia)
 * y correlativas son los ids de las materias requeridas previas.
 */
@Getter
@Setter
public class MateriaDTO {

    private Integer idMateria;
    private String codigo;
    private String nombre;
    private Integer idCarrera;
    private String carreraNombre;
    private Integer idPlan;
    private String planNombre;
    private Integer anio;
    private String regimen;
    private Integer horasSemanales;
    private List<Integer> correlativas;
}
