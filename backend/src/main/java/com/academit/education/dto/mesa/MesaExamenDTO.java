package com.academit.education.dto.mesa;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Response de mesa de examen según el contrato (MesaExamenDTO). Los campos
 * inscriptos, cuposDisponibles, nombres desnormalizados y
 * alumnosInscriptosIds se calculan, no se persisten.
 */
@Getter
@Setter
public class MesaExamenDTO {

    private Integer idMesa;

    private Integer idMateria;

    private String materiaNombre;

    private Integer idCarrera;

    private String carreraNombre;

    private String fecha;

    private String hora;

    /** MANANA, TARDE o NOCHE. */
    private String turno;

    private String tribunal;

    private String aula;

    /** Capacidad de la mesa (cupo). */
    private Integer cupoMaximo;

    /** Inscriptos activos, calculado. */
    private Integer inscriptos;

    /** cupoMaximo - inscriptos, calculado. */
    private Integer cuposDisponibles;

    /** Ids de alumno con inscripcion activa. */
    private List<Integer> alumnosInscriptosIds;

    /** ABIERTA, CERRADA o FINALIZADA. */
    private String estado;
}
