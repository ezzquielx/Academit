package com.academit.education.service;

import com.academit.education.dto.solicitud.SolicitudCreateDTO;
import com.academit.education.dto.solicitud.SolicitudDTO;
import com.academit.education.dto.solicitud.SolicitudResolverDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Alumno;
import com.academit.education.model.Rol;
import com.academit.education.model.Solicitud;
import com.academit.education.model.Usuario;
import com.academit.education.repository.SolicitudRepository;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.service.impl.SolicitudServiceImpl;
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
class SolicitudServiceTest {

    /** Reloj fijo: 15 de octubre de 2026. */
    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);
    private static final Clock RELOJ_FIJO = Clock.fixed(
            HOY.atStartOfDay(ZoneId.of("America/Argentina/Tucuman")).toInstant(),
            ZoneId.of("America/Argentina/Tucuman"));

    @Mock
    private SolicitudRepository solicitudRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private SolicitudService solicitudService;

    @BeforeEach
    void setUp() {
        solicitudService = new SolicitudServiceImpl(solicitudRepository, usuarioRepository, RELOJ_FIJO);
    }

    private Alumno alumno(Integer id) {
        Alumno a = new Alumno();
        a.setId(id);
        a.setNombre("Lucía");
        a.setApellido("Benítez");
        return a;
    }

    private Usuario usuario(String rol, Integer idAlumno) {
        Rol r = new Rol();
        r.setNombre(rol);
        Usuario u = new Usuario();
        u.setUsername("usuario");
        u.setRol(r);
        if (idAlumno != null) {
            u.setAlumno(alumno(idAlumno));
        }
        return u;
    }

    private Solicitud solicitud(Integer id, String tipo, String estado, LocalDate fecha) {
        Solicitud s = new Solicitud();
        s.setId(id);
        s.setAlumno(alumno(7));
        s.setTipo(tipo);
        s.setMotivo("Trámite de prueba");
        s.setEstado(estado);
        s.setFechaSolicitud(fecha);
        return s;
    }

    private SolicitudCreateDTO create(Integer idAlumno, String tipo) {
        return SolicitudCreateDTO.builder()
                .idAlumno(idAlumno)
                .tipo(tipo)
                .motivo("Necesito una constancia")
                .build();
    }

    // ------------------------------------------------------------------
    // GET /api/solicitudes (RF53)
    // ------------------------------------------------------------------

    @Test
    void listarAdminAplicaFiltrosDeTipoYEstado() {
        when(usuarioRepository.findByUsername("administrativo"))
                .thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(solicitudRepository.findAll()).thenReturn(List.of(
                solicitud(1, "CONSTANCIA", "PENDIENTE", LocalDate.of(2026, 10, 1)),
                solicitud(2, "PASANTIA", "APROBADA", LocalDate.of(2026, 10, 5)),
                solicitud(3, "CONSTANCIA", "APROBADA", LocalDate.of(2026, 10, 8))));

        List<SolicitudDTO> sinFiltros = solicitudService.listarSolicitudes("administrativo", null, null, null);
        List<SolicitudDTO> filtradas = solicitudService.listarSolicitudes("administrativo", null, "CONSTANCIA", "APROBADA");

        assertEquals(3, sinFiltros.size());
        // Más reciente primero.
        assertEquals(3, sinFiltros.get(0).getIdSolicitud());
        assertEquals(1, filtradas.size());
        assertEquals(3, filtradas.get(0).getIdSolicitud());
        assertEquals("Lucía Benítez", filtradas.get(0).getAlumnoNombre());
    }

    @Test
    void listarAdminConFiltroDeAlumno() {
        when(usuarioRepository.findByUsername("administrativo"))
                .thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(solicitudRepository.findByAlumnoId(7))
                .thenReturn(List.of(solicitud(1, "CONSTANCIA", "PENDIENTE", LocalDate.of(2026, 10, 1))));

        List<SolicitudDTO> solicitudes = solicitudService.listarSolicitudes("administrativo", 7, null, null);

        assertEquals(1, solicitudes.size());
        verify(solicitudRepository, never()).findAll();
    }

    @Test
    void alumnoSoloVeLasSuyas() {
        when(usuarioRepository.findByUsername("alumno1"))
                .thenReturn(Optional.of(usuario("ALUMNO", 7)));
        when(solicitudRepository.findByAlumnoId(7))
                .thenReturn(List.of(solicitud(1, "CONSTANCIA", "PENDIENTE", LocalDate.of(2026, 10, 1))));

        List<SolicitudDTO> solicitudes = solicitudService.listarSolicitudes("alumno1", null, null, null);

        assertEquals(1, solicitudes.size());
        assertEquals(7, solicitudes.get(0).getIdAlumno());
        verify(solicitudRepository, never()).findAll();
    }

    @Test
    void alumnoNoPuedeConsultarLasSolicitudesDeOtro() {
        when(usuarioRepository.findByUsername("alumno1"))
                .thenReturn(Optional.of(usuario("ALUMNO", 7)));

        assertThrows(AccessDeniedException.class,
                () -> solicitudService.listarSolicitudes("alumno1", 8, null, null));
        verify(solicitudRepository, never()).findAll();
    }

    @Test
    void alumnoSinPadronNoExponeDatos() {
        when(usuarioRepository.findByUsername("alumno9"))
                .thenReturn(Optional.of(usuario("ALUMNO", null)));

        assertTrue(solicitudService.listarSolicitudes("alumno9", null, null, null).isEmpty());
        verify(solicitudRepository, never()).findAll();
    }

    // ------------------------------------------------------------------
    // POST /api/solicitudes (RF54)
    // ------------------------------------------------------------------

    @Test
    void crearSolicitudPropiaNacePendiente() {
        when(usuarioRepository.findByUsername("alumno1"))
                .thenReturn(Optional.of(usuario("ALUMNO", 7)));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(inv -> {
            Solicitud s = inv.getArgument(0);
            s.setId(9);
            return s;
        });

        SolicitudDTO dto = solicitudService.crearSolicitud("alumno1", create(7, "CONSTANCIA"));

        assertEquals(9, dto.getIdSolicitud());
        assertEquals(7, dto.getIdAlumno());
        assertEquals("PENDIENTE", dto.getEstado());
        assertEquals(HOY, dto.getFechaSolicitud());
        assertNull(dto.getFechaResolucion());
        assertNull(dto.getRespuesta());
        verify(solicitudRepository).save(any(Solicitud.class));
    }

    @Test
    void crearSolicitudANombreDeOtroAlumnoLanza403() {
        when(usuarioRepository.findByUsername("alumno1"))
                .thenReturn(Optional.of(usuario("ALUMNO", 7)));

        assertThrows(AccessDeniedException.class,
                () -> solicitudService.crearSolicitud("alumno1", create(8, "CONSTANCIA")));
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void crearSolicitudSinAlumnoVinculadoLanza403() {
        when(usuarioRepository.findByUsername("alumno1"))
                .thenReturn(Optional.of(usuario("ALUMNO", null)));

        assertThrows(AccessDeniedException.class,
                () -> solicitudService.crearSolicitud("alumno1", create(7, "CONSTANCIA")));
        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void crearSolicitudConRolNoAlumnoLanza403() {
        when(usuarioRepository.findByUsername("administrativo"))
                .thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));

        assertThrows(AccessDeniedException.class,
                () -> solicitudService.crearSolicitud("administrativo", create(7, "CONSTANCIA")));
        verify(solicitudRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // PATCH /api/solicitudes/{id} (RF55)
    // ------------------------------------------------------------------

    @Test
    void resolverAprobadaRegistraFechaYRespuestaPorDefecto() {
        Solicitud s = solicitud(1, "CONSTANCIA", "PENDIENTE", LocalDate.of(2026, 10, 1));
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(s));

        SolicitudDTO dto = solicitudService.resolverSolicitud(1,
                SolicitudResolverDTO.builder().estado("APROBADA").build());

        assertEquals("APROBADA", dto.getEstado());
        assertEquals(HOY, dto.getFechaResolucion());
        assertEquals("Solicitud aprobada por el área administrativa", dto.getRespuesta());
        verify(solicitudRepository).save(s);
    }

    @Test
    void resolverConObservacionUsaElTextoDelAdministrador() {
        Solicitud s = solicitud(1, "PASANTIA", "PENDIENTE", LocalDate.of(2026, 10, 1));
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(s));

        SolicitudDTO dto = solicitudService.resolverSolicitud(1,
                SolicitudResolverDTO.builder().estado("RECHAZADA").observacion("Falta el seguro").build());

        assertEquals("RECHAZADA", dto.getEstado());
        assertEquals("Falta el seguro", dto.getRespuesta());
    }

    @Test
    void resolverRechazadaSinObservacionUsaTextoPorDefecto() {
        Solicitud s = solicitud(1, "CONSTANCIA", "PENDIENTE", LocalDate.of(2026, 10, 1));
        when(solicitudRepository.findById(1)).thenReturn(Optional.of(s));

        SolicitudDTO dto = solicitudService.resolverSolicitud(1,
                SolicitudResolverDTO.builder().estado("RECHAZADA").build());

        assertEquals("Solicitud rechazada", dto.getRespuesta());
    }

    @Test
    void resolverSolicitudInexistenteLanza404() {
        when(solicitudRepository.findById(44)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> solicitudService.resolverSolicitud(44,
                SolicitudResolverDTO.builder().estado("APROBADA").build()));
        verify(solicitudRepository, never()).save(any());
    }
}
