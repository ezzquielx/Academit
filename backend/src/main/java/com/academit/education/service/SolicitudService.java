package com.academit.education.service;

import com.academit.education.dto.solicitud.SolicitudCreateDTO;
import com.academit.education.dto.solicitud.SolicitudDTO;
import com.academit.education.dto.solicitud.SolicitudResolverDTO;

import java.util.List;

/**
 * Solicitudes y trámites (RF53-RF55, RN17).
 * La declaración jurada NO es un tipo de solicitud (módulo propio).
 */
public interface SolicitudService {

    /** Listado con filtros opcionales; un alumno solo consulta las suyas. */
    List<SolicitudDTO> listarSolicitudes(String username, Integer idAlumno, String tipo, String estado);

    /** Crea una solicitud propia del alumno, siempre en PENDIENTE (201). */
    SolicitudDTO crearSolicitud(String username, SolicitudCreateDTO request);

    /** Resuelve una solicitud desde la bandeja administrativa. */
    SolicitudDTO resolverSolicitud(Integer idSolicitud, SolicitudResolverDTO request);
}
