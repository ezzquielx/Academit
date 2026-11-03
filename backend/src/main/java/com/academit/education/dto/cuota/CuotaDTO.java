package com.academit.education.dto.cuota;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CuotaDTO del contrato (docs/frontend/openapi.yaml).
 * idAlumno, alumnoNombre, concepto, mes, anio, tipo y estado son campos
 * calculados a partir de la matricula y del catalogo de tipos (RF28, RN18).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuotaDTO {

    private Integer idCuota;
    private Integer idMatricula;
    private Integer idAlumno;
    private String alumnoNombre;
    private String concepto;
    private Integer mes;
    private Integer anio;
    private LocalDate fechaVencimiento;
    private BigDecimal importe;
    private String tipo;
    private String estado;
    private LocalDate fechaPago;
}
