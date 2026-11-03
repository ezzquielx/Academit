package com.academit.education.dto.asistencia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request del lote de asistencia según el contrato (AsistenciasLoteDTO) para
 * POST /api/asistencias/lote. RN15: la asistencia se registra por alumno,
 * curso, materia y fecha; si ya existe se actualiza (upsert). El lote es
 * atomico: si un item es invalido, 400 con detalle por posicion y no se
 * guarda nada.
 */
@Getter
@Setter
public class AsistenciasLoteDTO {

    @NotEmpty(message = "El lote de asistencias es obligatorio")
    @Valid
    private List<Item> asistencias;

    @Getter
    @Setter
    public static class Item {

        @NotNull(message = "El alumno es obligatorio")
        private Integer idAlumno;

        @NotNull(message = "La materia es obligatoria")
        private Integer idMateria;

        @NotNull(message = "El curso es obligatorio")
        private Integer idCurso;

        /** YYYY-MM-DD; no puede ser futura (RN15). */
        @NotBlank(message = "La fecha es obligatoria")
        private String fecha;

        /** PRESENTE, AUSENTE, TARDANZA o JUSTIFICADO. */
        @NotBlank(message = "El estado es obligatorio")
        private String estado;
    }
}
