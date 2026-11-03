package com.academit.education.dto.alumno;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Situación administrativa y arancelaria del alumno según el contrato OpenAPI.
 */
@Getter
@Setter
@NoArgsConstructor
public class SituacionAdministrativaDTO {

    private Integer idAlumno;
    private Integer cuotasPagas = 0;
    private Integer cuotasPendientes = 0;
    private Double totalDeuda = 0.0;
    private Double totalAbonado = 0.0;
    private String matricula;
    private String estadoPago;
    private List<CuotaDetalleDTO> cuotas = new ArrayList<>();
}

