package com.academit.education.dto.pago;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * PagoCreateDTO del contrato (docs/frontend/openapi.yaml).
 * Registra el pago de UNA cuota por su importe completo (RN09).
 * medioPago es opcional: si no viene se usa EFECTIVO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoCreateDTO {

    @NotNull(message = "El id de la cuota es obligatorio")
    private Integer idCuota;

    @NotNull(message = "El importe es obligatorio")
    @Positive(message = "El importe debe ser mayor a cero")
    private BigDecimal importe;

    @Pattern(regexp = "^(EFECTIVO|TRANSFERENCIA|TARJETA|TARJETA_CREDITO|TARJETA_DEBITO|OTRO)$",
            message = "El medio de pago debe ser EFECTIVO, TRANSFERENCIA, TARJETA, TARJETA_CREDITO, TARJETA_DEBITO u OTRO")
    private String medioPago;
}
