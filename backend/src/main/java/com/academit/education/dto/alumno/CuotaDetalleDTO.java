package com.academit.education.dto.alumno;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Detalle individual de cuota dentro de la situación administrativa.
 */
@Getter
@Setter
@NoArgsConstructor
public class CuotaDetalleDTO {

    private Integer idCuota;
    private String concepto;
    private String tipo;
    private String periodo;
    private BigDecimal importe;
    private LocalDate fechaVencimiento;
    private String estado;
    private String comprobante;
}

