package com.academit.education.dto.alumno;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO de respuesta para la entidad Alumno según el contrato de frontend (openapi.yaml).
 */
@Getter
@Setter
@NoArgsConstructor
public class AlumnoDTO {

    private Integer idAlumno;
    private String legajo;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private String domicilio;
    private LocalDate fechaNacimiento;
    private Integer idCarrera;
    private String carrera;
    private Integer idPlan;
    private String planNombre;
    private Integer idCurso;
    private String cursoNombre;
    private Integer cicloLectivo;
    private String estado;
    private String estadoMatricula;
    private Integer idMatricula;
}

