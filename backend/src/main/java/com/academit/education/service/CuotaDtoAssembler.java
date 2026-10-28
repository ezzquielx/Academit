package com.academit.education.service;

import com.academit.education.dto.cuota.CuotaDTO;
import com.academit.education.model.Cuota;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Convierte una {@link Cuota} en su DTO de contrato.
 *
 * Campos calculados (nunca se persisten):
 * - idAlumno / alumnoNombre: derivados de la matricula de la cuota.
 * - concepto, mes y anio: la tabla cuota no guarda descripcion ni periodo,
 *   se reconstruyen desde numero, tipo y fecha de vencimiento.
 * - tipo: el catalogo de la base usa CUOTA_MENSUAL y el contrato MENSUAL.
 * - estado: una cuota PENDIENTE vencida se muestra como VENCIDA aunque la
 *   tarea programada todavia no la haya persistido (RN07, RF30).
 */
@Component
public class CuotaDtoAssembler {

    private static final String[] MESES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };

    private final Clock clock;

    public CuotaDtoAssembler(Clock clock) {
        this.clock = clock;
    }

    public CuotaDTO toDto(Cuota cuota) {
        return toDto(cuota, null);
    }

    public CuotaDTO toDto(Cuota cuota, LocalDate fechaPago) {
        var matricula = cuota.getMatricula();
        var alumno = matricula != null ? matricula.getAlumno() : null;

        return CuotaDTO.builder()
                .idCuota(cuota.getId())
                .idMatricula(matricula != null ? matricula.getId() : null)
                .idAlumno(alumno != null ? alumno.getId() : null)
                .alumnoNombre(alumno != null ? alumno.getNombre() + " " + alumno.getApellido() : null)
                .concepto(concepto(cuota))
                .mes(cuota.getFechaVencimiento() != null ? cuota.getFechaVencimiento().getMonthValue() : null)
                .anio(cuota.getFechaVencimiento() != null ? cuota.getFechaVencimiento().getYear() : null)
                .fechaVencimiento(cuota.getFechaVencimiento())
                .importe(cuota.getImporte())
                .tipo(tipoContrato(cuota))
                .estado(estadoVisible(cuota))
                .fechaPago(fechaPago)
                .build();
    }

    /** Concepto arancelario: "Cuota 1 - Enero 2026" o "Seguro Cuatrimestral 1/3". */
    public String concepto(Cuota cuota) {
        String tipo = cuota.getTipo() != null ? cuota.getTipo().getNombre() : "";
        Integer numero = cuota.getNumero();
        LocalDate vencimiento = cuota.getFechaVencimiento();

        if ("SEGURO_CUATRIMESTRAL".equals(tipo)) {
            return "Seguro Cuatrimestral " + (numero != null ? numero : 1) + "/3";
        }
        if (vencimiento != null) {
            return "Cuota " + (numero != null ? numero : vencimiento.getMonthValue())
                    + " - " + MESES[vencimiento.getMonthValue() - 1] + " " + vencimiento.getYear();
        }
        return tipo;
    }

    /** El catalogo de la base guarda CUOTA_MENSUAL; el contrato expone MENSUAL. */
    public String tipoContrato(Cuota cuota) {
        String tipo = cuota.getTipo() != null ? cuota.getTipo().getNombre() : null;
        if ("CUOTA_MENSUAL".equals(tipo)) {
            return "MENSUAL";
        }
        return tipo;
    }

    /** Estado visible: PENDIENTE vencida (vencimiento anterior a hoy) se muestra VENCIDA. */
    public String estadoVisible(Cuota cuota) {
        String estado = cuota.getEstado() != null ? cuota.getEstado().getNombre() : null;
        LocalDate hoy = LocalDate.now(clock);

        if ("PENDIENTE".equals(estado)
                && cuota.getFechaVencimiento() != null
                && cuota.getFechaVencimiento().isBefore(hoy)) {
            return "VENCIDA";
        }
        return estado;
    }
}
