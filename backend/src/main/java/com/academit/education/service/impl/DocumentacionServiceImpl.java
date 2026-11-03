package com.academit.education.service.impl;

import com.academit.education.dto.documentacion.DocumentacionDTO;
import com.academit.education.dto.documentacion.DocumentacionUpdateDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Documentacion;
import com.academit.education.model.Usuario;
import com.academit.education.repository.DocumentacionRepository;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.service.DocumentacionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Legajo documental (RF56-RF58, RN17).
 * El alumno consulta solo su legajo; ADMINISTRATIVO y ADMINISTRADOR
 * ven todo y actualizan con PUT (unica escritura del contrato).
 */
@Service
public class DocumentacionServiceImpl implements DocumentacionService {

    private static final String ROL_ALUMNO = "ALUMNO";
    private static final String ESTADO_ENTREGADO = "ENTREGADO";

    private final DocumentacionRepository documentacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public DocumentacionServiceImpl(DocumentacionRepository documentacionRepository,
                                    UsuarioRepository usuarioRepository,
                                    Clock clock) {
        this.documentacionRepository = documentacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentacionDTO> listarDocumentacion(String username, Integer idAlumno) {
        Usuario usuario = buscarUsuario(username);
        List<Documentacion> documentos;

        if (esAlumno(usuario)) {
            Integer idPropio = idAlumnoPropio(usuario);
            if (idPropio == null) {
                return List.of();
            }
            if (idAlumno != null && !idPropio.equals(idAlumno)) {
                throw new AccessDeniedException("Un alumno solo puede consultar su propia documentación");
            }
            documentos = documentacionRepository.findByAlumnoId(idPropio);
        } else {
            documentos = idAlumno != null
                    ? documentacionRepository.findByAlumnoId(idAlumno)
                    : documentacionRepository.findAll();
        }

        return documentos.stream()
                .sorted(Comparator.comparing(Documentacion::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public DocumentacionDTO actualizarDocumentacion(Integer idDocumento, DocumentacionUpdateDTO request) {
        Documentacion documento = documentacionRepository.findById(idDocumento)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + idDocumento));

        documento.setEstado(request.getEstado());
        documento.setObservaciones(request.getObservaciones());

        // RN17: la fecha de entrega acompana al estado ENTREGADO; si se marca
        // ENTREGADO sin fecha se registra hoy, y al sacarlo se limpia.
        if (ESTADO_ENTREGADO.equalsIgnoreCase(request.getEstado())) {
            documento.setFechaEntrega(request.getFechaEntrega() != null
                    ? request.getFechaEntrega()
                    : LocalDate.now(clock));
        } else {
            documento.setFechaEntrega(null);
        }

        documentacionRepository.save(documento);
        return toDto(documento);
    }

    private DocumentacionDTO toDto(Documentacion documento) {
        var alumno = documento.getAlumno();
        return DocumentacionDTO.builder()
                .idDocumento(documento.getId())
                .idAlumno(alumno != null ? alumno.getId() : null)
                .alumnoNombre(alumno != null ? alumno.getNombre() + " " + alumno.getApellido() : null)
                .tipo(documento.getTipo())
                .obligatorio(documento.getObligatorio())
                .estado(documento.getEstado())
                .fechaEntrega(documento.getFechaEntrega())
                .observaciones(documento.getObservaciones())
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
