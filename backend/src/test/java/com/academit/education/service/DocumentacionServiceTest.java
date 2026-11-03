package com.academit.education.service;

import com.academit.education.dto.documentacion.DocumentacionDTO;
import com.academit.education.dto.documentacion.DocumentacionUpdateDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Alumno;
import com.academit.education.model.Documentacion;
import com.academit.education.model.Rol;
import com.academit.education.model.Usuario;
import com.academit.education.repository.DocumentacionRepository;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.service.impl.DocumentacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentacionServiceTest {

    /** Reloj fijo: 15 de octubre de 2026. */
    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);
    private static final Clock RELOJ_FIJO = Clock.fixed(
            HOY.atStartOfDay(ZoneId.of("America/Argentina/Tucuman")).toInstant(),
            ZoneId.of("America/Argentina/Tucuman"));

    @Mock
    private DocumentacionRepository documentacionRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private DocumentacionService documentacionService;

    @BeforeEach
    void setUp() {
        documentacionService = new DocumentacionServiceImpl(
                documentacionRepository, usuarioRepository, RELOJ_FIJO);
    }

    private Documentacion documento(Integer id, Integer idAlumno, String estado) {
        Alumno alumno = new Alumno();
        alumno.setId(idAlumno);
        alumno.setNombre("Ana");
        alumno.setApellido("Perez");

        Documentacion d = new Documentacion();
        d.setId(id);
        d.setAlumno(alumno);
        d.setTipo("DNI");
        d.setObligatorio(true);
        d.setEstado(estado);
        return d;
    }

    private Usuario usuario(String rol, Integer idAlumno) {
        Rol r = new Rol();
        r.setNombre(rol);
        Usuario u = new Usuario();
        u.setUsername("usuario");
        u.setRol(r);
        if (idAlumno != null) {
            Alumno a = new Alumno();
            a.setId(idAlumno);
            u.setAlumno(a);
        }
        return u;
    }

    // ------------------------------------------------------------------
    // GET /api/documentacion (RF56)
    // ------------------------------------------------------------------

    @Test
    void listarComoAdminDevuelveTodoConAlumnoNombre() {
        when(usuarioRepository.findByUsername("administrativo"))
                .thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(documentacionRepository.findAll())
                .thenReturn(List.of(documento(1, 7, "ENTREGADO"), documento(2, 7, "PENDIENTE")));

        List<DocumentacionDTO> documentos = documentacionService.listarDocumentacion("administrativo", null);

        assertEquals(2, documentos.size());
        assertEquals("Ana Perez", documentos.get(0).getAlumnoNombre());
        assertEquals("DNI", documentos.get(0).getTipo());
        assertEquals("PENDIENTE", documentos.get(1).getEstado());
    }

    @Test
    void listarConFiltroDeAlumno() {
        when(usuarioRepository.findByUsername("administrativo"))
                .thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(documentacionRepository.findByAlumnoId(7))
                .thenReturn(List.of(documento(1, 7, "PENDIENTE")));

        List<DocumentacionDTO> documentos = documentacionService.listarDocumentacion("administrativo", 7);

        assertEquals(1, documentos.size());
        verify(documentacionRepository, never()).findAll();
    }

    @Test
    void alumnoSoloVeSuLegajo() {
        when(usuarioRepository.findByUsername("alumno1"))
                .thenReturn(Optional.of(usuario("ALUMNO", 7)));
        when(documentacionRepository.findByAlumnoId(7))
                .thenReturn(List.of(documento(1, 7, "RECHAZADO")));

        List<DocumentacionDTO> documentos = documentacionService.listarDocumentacion("alumno1", null);

        assertEquals(1, documentos.size());
        assertEquals(7, documentos.get(0).getIdAlumno());
        verify(documentacionRepository, never()).findAll();
    }

    @Test
    void alumnoNoPuedeConsultarElLegajoDeOtro() {
        when(usuarioRepository.findByUsername("alumno1"))
                .thenReturn(Optional.of(usuario("ALUMNO", 7)));

        assertThrows(AccessDeniedException.class,
                () -> documentacionService.listarDocumentacion("alumno1", 8));
        verify(documentacionRepository, never()).findAll();
    }

    @Test
    void alumnoSinPadronNoExponeDatos() {
        when(usuarioRepository.findByUsername("alumno9"))
                .thenReturn(Optional.of(usuario("ALUMNO", null)));

        assertTrue(documentacionService.listarDocumentacion("alumno9", null).isEmpty());
        verify(documentacionRepository, never()).findAll();
    }

    // ------------------------------------------------------------------
    // PUT /api/documentacion/{id} (RF57, RF58)
    // ------------------------------------------------------------------

    @Test
    void actualizarCambiaEstadoYObservaciones() {
        Documentacion d = documento(1, 7, "PENDIENTE");
        when(documentacionRepository.findById(1)).thenReturn(Optional.of(d));

        DocumentacionDTO dto = documentacionService.actualizarDocumentacion(1,
                DocumentacionUpdateDTO.builder()
                        .estado("RECHAZADO")
                        .observaciones("Falta sellado")
                        .build());

        assertEquals("RECHAZADO", dto.getEstado());
        assertEquals("Falta sellado", dto.getObservaciones());
        assertNull(dto.getFechaEntrega());
        verify(documentacionRepository).save(d);
    }

    @Test
    void marcarEntregadoSinFechaUsaHoy() {
        Documentacion d = documento(1, 7, "PENDIENTE");
        when(documentacionRepository.findById(1)).thenReturn(Optional.of(d));

        DocumentacionDTO dto = documentacionService.actualizarDocumentacion(1,
                DocumentacionUpdateDTO.builder().estado("ENTREGADO").build());

        assertEquals("ENTREGADO", dto.getEstado());
        assertEquals(HOY, dto.getFechaEntrega());
    }

    @Test
    void marcarEntregadoConFechaRespetaElValorEnviado() {
        Documentacion d = documento(1, 7, "PENDIENTE");
        when(documentacionRepository.findById(1)).thenReturn(Optional.of(d));

        DocumentacionDTO dto = documentacionService.actualizarDocumentacion(1,
                DocumentacionUpdateDTO.builder()
                        .estado("ENTREGADO")
                        .fechaEntrega(LocalDate.of(2026, 9, 1))
                        .build());

        assertEquals(LocalDate.of(2026, 9, 1), dto.getFechaEntrega());
    }

    @Test
    void sacarEntregadoLimpiaLaFechaDeEntrega() {
        Documentacion d = documento(1, 7, "ENTREGADO");
        d.setFechaEntrega(LocalDate.of(2026, 9, 1));
        when(documentacionRepository.findById(1)).thenReturn(Optional.of(d));

        DocumentacionDTO dto = documentacionService.actualizarDocumentacion(1,
                DocumentacionUpdateDTO.builder().estado("PENDIENTE").build());

        assertEquals("PENDIENTE", dto.getEstado());
        assertNull(dto.getFechaEntrega());
    }

    @Test
    void documentoInexistenteLanza404() {
        when(documentacionRepository.findById(44)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> documentacionService.actualizarDocumentacion(44,
                DocumentacionUpdateDTO.builder().estado("ENTREGADO").build()));
        verify(documentacionRepository, never()).save(any());
    }
}
