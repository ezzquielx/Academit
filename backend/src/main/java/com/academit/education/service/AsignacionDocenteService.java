package com.academit.education.service;

import com.academit.education.security.CustomUserDetails;

import java.util.List;

/**
 * Componente reutilizable «docente asignado a la cátedra» (RN11).
 * Lo usan evaluaciones, calificaciones y asistencias: el docente del token
 * solo puede escribir sobre cátedras (curso_materia) donde esté asignado y
 * consulta solo las suyas.
 */
public interface AsignacionDocenteService {

    /** Id del docente asociado al usuario del token, o null si no lo tiene. */
    Integer idDocente(CustomUserDetails userDetails);

    /** True si el token actúa como ADMINISTRADOR. */
    boolean esAdministrador(CustomUserDetails userDetails);

    /** True si el docente del token está asignado a la cátedra (RN11). */
    boolean estaAsignado(Integer idDocente, Integer idCurso, Integer idMateria);

    /** RN11: si el docente no está asignado a la cátedra, lanza 403. */
    void verificarAsignado(CustomUserDetails userDetails, Integer idCurso, Integer idMateria);

    /** Ids de cátedra (curso_materia) del docente del token; vacío si no es docente. */
    List<Integer> catedrasDelDocente(CustomUserDetails userDetails);
}
