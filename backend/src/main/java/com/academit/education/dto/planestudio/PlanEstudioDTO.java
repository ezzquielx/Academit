package com.academit.education.dto.planestudio;

import lombok.Getter;
import lombok.Setter;

/**
 * Response de plan de estudios según el contrato (PlanEstudioDTO del openapi).
 * cantidadMaterias es un campo calculado (no persistido). Lo comparten los
 * módulos carreras y planes-materias.
 */
@Getter
@Setter
public class PlanEstudioDTO {

    private Integer idPlan;
    private Integer idCarrera;
    private String carreraNombre;
    private String codigo;
    private String nombre;
    private Integer anioAprobacion;
    private Integer cantidadMaterias;
    private String estado;
}
