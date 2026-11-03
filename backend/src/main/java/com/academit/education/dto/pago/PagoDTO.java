package com.academit.education.dto.pago;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * PagoDTO del contrato (docs/frontend/openapi.yaml).
 * idAlumno, alumnoNombre, concepto y estado son campos calculados:
 * la tabla pago no los guarda y siempre se registra acreditado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDTO {

    private Integer idPago;
    private Integer idCuota;
    private Integer idAlumno;
    private String alumnoNombre;
    private String concepto;
    private BigDecimal importe;
    private LocalDate fechaPago;
    private String medioPago;
    private String comprobante;
    private String estado;
}
