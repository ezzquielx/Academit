package com.academit.education.service.impl;

import com.academit.education.dto.solicitud.SolicitudCreateDTO;
import com.academit.education.dto.solicitud.SolicitudDTO;
import com.academit.education.dto.solicitud.SolicitudResolverDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Solicitud;
import com.academit.education.model.Usuario;
import com.academit.education.repository.SolicitudRepository;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.service.SolicitudService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Solicitudes y trámites (RF53-RF55, RN17).
 * El alumno crea y consulta solo las suyas; la resolución es
 * exclusiva de ADMINISTRATIVO y ADMINISTRADOR.
 */
@Service
public class SolicitudServiceImpl implements SolicitudService {

    private static final String ROL_ALUMNO = "ALUMNO";
    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_APROBADA = "APROBADA";
    private static final String ESTADO_RECHAZADA = "RECHAZADA";

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public SolicitudServiceImpl(SolicitudRepository solicitudRepository,
                                UsuarioRepository usuarioRepository,
                                Clock clock) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudDTO> listarSolicitudes(String username, Integer idAlumno, String tipo, String estado) {
        Usuario usuario = buscarUsuario(username);
        List<Solicitud> solicitudes;

        if (esAlumno(usuario)) {
            Integer idPropio = idAlumnoPropio(usuario);
            if (idPropio == null) {
                return List.of();
            }
            if (idAlumno != null && !idPropio.equals(idAlumno)) {
                throw new AccessDeniedException("Un alumno solo puede consultar sus propias solicitudes");
            }
            solicitudes = solicitudRepository.findByAlumnoId(idPropio);
        } else {
            solicitudes = idAlumno != null
                    ? solicitudRepository.findByAlumnoId(idAlumno)
                    : solicitudRepository.findAll();
        }

        final String tipoFiltro = tipo;
        final String estadoFiltro = estado;
        return solicitudes.stream()
                .filter(s -> tipoFiltro == null || tipoFiltro.isBlank() || tipoFiltro.equals(s.getTipo()))
                .filter(s -> estadoFiltro == null || estadoFiltro.isBlank() || estadoFiltro.equals(s.getEstado()))
                .sorted(Comparator.comparing(Solicitud::getFechaSolicitud,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Solicitud::getId, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SolicitudDTO crearSolicitud(String username, SolicitudCreateDTO request) {
        Usuario usuario = buscarUsuario(username);
        if (!esAlumno(usuario) || usuario.getAlumno() == null) {
            throw new AccessDeniedException("Solo un alumno con padrón activo puede crear solicitudes");
        }
        if (!usuario.getAlumno().getId().equals(request.getIdAlumno())) {
            throw new AccessDeniedException("Un alumno solo puede crear solicitudes a su nombre");
        }

        Solicitud solicitud = new Solicitud();
        solicitud.setAlumno(usuario.getAlumno());
        solicitud.setTipo(request.getTipo());
        solicitud.setMotivo(request.getMotivo());
        solicitud.setObservaciones(request.getObservaciones());
        // Toda solicitud nueva nace PENDIENTE y sin resolución.
        solicitud.setEstado(ESTADO_PENDIENTE);
        solicitud.setFechaSolicitud(LocalDate.now(clock));
        solicitud.setFechaResolucion(null);
        solicitud.setRespuesta(null);

        solicitudRepository.save(solicitud);
        return toDto(solicitud);
    }

    @Override
    @Transactional
    public SolicitudDTO resolverSolicitud(Integer idSolicitud, SolicitudResolverDTO request) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con ID: " + idSolicitud));

        solicitud.setEstado(request.getEstado());
        solicitud.setFechaResolucion(LocalDate.now(clock));
        solicitud.setRespuesta(respuesta(request));

        solicitudRepository.save(solicitud);
        return toDto(solicitud);
    }

    /** La observación del administrador es la respuesta; si no viene se usa el texto por defecto. */
    private String respuesta(SolicitudResolverDTO request) {
        if (request.getObservacion() != null && !request.getObservacion().isBlank()) {
            return request.getObservacion();
        }
        if (ESTADO_APROBADA.equalsIgnoreCase(request.getEstado())) {
            return "Solicitud aprobada por el área administrativa";
        }
        if (ESTADO_RECHAZADA.equalsIgnoreCase(request.getEstado())) {
            return "Solicitud rechazada";
        }
        return null;
    }

    private SolicitudDTO toDto(Solicitud solicitud) {
        var alumno = solicitud.getAlumno();
        return SolicitudDTO.builder()
                .idSolicitud(solicitud.getId())
                .idAlumno(alumno != null ? alumno.getId() : null)
                .alumnoNombre(alumno != null ? alumno.getNombre() + " " + alumno.getApellido() : null)
                .tipo(solicitud.getTipo())
                .fechaSolicitud(solicitud.getFechaSolicitud())
                .motivo(solicitud.getMotivo())
                .observaciones(solicitud.getObservaciones())
                .estado(solicitud.getEstado())
                .fechaResolucion(solicitud.getFechaResolucion())
                .respuesta(solicitud.getRespuesta())
                .build();
    }

    private Usuario buscarUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private boolean esAlumno(Usuario usuario) {
        return usuario.getRol() != null && ROL_ALUMNO.equalsIgnoreCase(usuario.getRol().getNombre());
    }

    private Integer idAlumnoPropio(Usuario usuario) {
        return usuario.getAlumno() != null ? usuario.getAlumno().getId() : null;
    }
}
