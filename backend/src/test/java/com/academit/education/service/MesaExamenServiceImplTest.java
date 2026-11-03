package com.academit.education.service;

import com.academit.education.dto.mesa.MesaExamenCreateDTO;
import com.academit.education.dto.mesa.MesaExamenDTO;
import com.academit.education.dto.mesa.MesaExamenUpdateDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Alumno;
import com.academit.education.model.Carrera;
import com.academit.education.model.EstadoPersona;
import com.academit.education.model.InscripcionMesa;
import com.academit.education.model.Materia;
import com.academit.education.model.MesaExamen;
import com.academit.education.model.PlanEstudio;
import com.academit.education.model.PlanMateria;
import com.academit.education.model.PlanMateriaId;
import com.academit.education.model.Usuario;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.CarreraRepository;
import com.academit.education.repository.InscripcionMesaRepository;
import com.academit.education.repository.MateriaRepository;
import com.academit.education.repository.MesaExamenRepository;
import com.academit.education.repository.PlanMateriaRepository;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.impl.MesaExamenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MesaExamenServiceImplTest {

    @Mock
    private MesaExamenRepository mesaExamenRepository;
    @Mock
    private InscripcionMesaRepository inscripcionMesaRepository;
    @Mock
    private MateriaRepository materiaRepository;
    @Mock
    private CarreraRepository carreraRepository;
    @Mock
    private PlanMateriaRepository planMateriaRepository;
    @Mock
    private AlumnoRepository alumnoRepository;

    private MesaExamenServiceImpl service;

    private Materia materia;
    private Carrera carrera;
    private MesaExamen mesa;
    private Alumno alumno;
    private CustomUserDetails alumnoSesion;
    private CustomUserDetails adminSesion;
    private CustomUserDetails docenteSesion;

    private Usuario usuarioDeRol(String rol, Alumno alu) {
        Usuario u = new Usuario();
        u.setRol(new com.academit.education.model.Rol());
        u.getRol().setNombre(rol);
        u.setAlumno(alu);
        return u;
    }

    private PlanMateria planDeLaMateria() {
        PlanEstudio plan = new PlanEstudio();
        plan.setId(5);
        plan.setCarrera(carrera);
        PlanMateria pm = new PlanMateria();
        PlanMateriaId pid = new PlanMateriaId();
        pid.setIdPlan(5);
        pid.setIdMateria(materia.getId());
        pm.setId(pid);
        pm.setPlan(plan);
        pm.setMateria(materia);
        return pm;
    }

    @BeforeEach
    void setUp() {
        service = new MesaExamenServiceImpl(mesaExamenRepository, inscripcionMesaRepository,
                materiaRepository, carreraRepository, planMateriaRepository, alumnoRepository);

        carrera = new Carrera();
        carrera.setId(1);
        carrera.setNombre("Tecnicatura Universitaria en Programación");

        materia = new Materia();
        materia.setId(9);
        materia.setNombre("Programación II");

        EstadoPersona estadoActivo = new EstadoPersona();
        estadoActivo.setId(1);
        estadoActivo.setNombre("ACTIVO");

        alumno = new Alumno();
        alumno.setId(7);
        alumno.setNombre("Lucía");
        alumno.setApellido("Pérez");
        alumno.setEstado(estadoActivo);

        mesa = new MesaExamen();
        mesa.setId(3);
        mesa.setMateria(materia);
        mesa.setFecha(LocalDate.now().plusDays(10));
        mesa.setCupo(30);
        mesa.setTurno("MANANA");
        mesa.setEstado("ABIERTA");

        alumnoSesion = new CustomUserDetails(usuarioDeRol("ALUMNO", alumno));
        adminSesion = new CustomUserDetails(usuarioDeRol("ADMINISTRADOR", null));
        docenteSesion = new CustomUserDetails(usuarioDeRol("DOCENTE", null));

        when(planMateriaRepository.findByMateriaId(9)).thenReturn(List.of(planDeLaMateria()));
        when(materiaRepository.findById(9)).thenReturn(Optional.of(materia));
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));
        when(inscripcionMesaRepository.countByMesaIdAndActivaTrue(3)).thenReturn(0L);
        when(inscripcionMesaRepository.findByMesaIdAndActivaTrue(3)).thenReturn(List.of());
    }

    private MesaExamenCreateDTO createDTO() {
        MesaExamenCreateDTO r = new MesaExamenCreateDTO();
        r.setIdMateria(9);
        r.setIdCarrera(1);
        r.setFecha(LocalDate.now().plusDays(30).toString());
        r.setHora("09:00 hs");
        r.setTurno("MANANA");
        r.setTribunal("Prof. Carlos Gómez");
        r.setAula("Aula Magna 1");
        r.setCupoMaximo(30);
        return r;
    }

    // ------------------------------------------------------------------
    // Alta y edición (ADMINISTRADOR)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("crearMesa:happy con carrera desnormalizada y estado ABIERTA")
    void crearMesa() {
        when(mesaExamenRepository.save(any(MesaExamen.class))).thenAnswer(inv -> {
            MesaExamen m = inv.getArgument(0);
            m.setId(3);
            return m;
        });

        MesaExamenDTO dto = service.crear(createDTO());

        assertEquals("ABIERTA", dto.getEstado());
        assertEquals("Programación II", dto.getMateriaNombre());
        assertEquals("Tecnicatura Universitaria en Programación", dto.getCarreraNombre());
        assertEquals(30, dto.getCupoMaximo());
    }

    @Test
    @DisplayName("crearMesa: materia de otra carrera → 400")
    void crearMesaMateriaDeOtraCarrera() {
        Carrera otra = new Carrera();
        otra.setId(2);
        otra.setNombre("Otra Carrera");
        when(carreraRepository.findById(2)).thenReturn(Optional.of(otra));

        MesaExamenCreateDTO r = createDTO();
        r.setIdCarrera(2);
        assertThrows(BusinessRuleException.class, () -> service.crear(r));
    }

    @Test
    @DisplayName("crearMesa: turno fuera del catálogo → 400")
    void crearMesaTurnoInvalido() {
        MesaExamenCreateDTO r = createDTO();
        r.setTurno("VESPERTINO");
        assertThrows(BusinessRuleException.class, () -> service.crear(r));
    }

    @Test
    @DisplayName("actualizarMesa: estado Finalizada y header del cupo")
    void actualizarMesa() {
        when(mesaExamenRepository.findById(3)).thenReturn(Optional.of(mesa));
        when(mesaExamenRepository.save(any(MesaExamen.class))).thenAnswer(inv -> inv.getArgument(0));

        MesaExamenUpdateDTO r = new MesaExamenUpdateDTO();
        r.setEstado("CERRADA");
        r.setCupoMaximo(25);

        MesaExamenDTO dto = service.actualizar(3, r);

        assertEquals("CERRADA", dto.getEstado());
        assertEquals(25, dto.getCupoMaximo());
    }

    @Test
    @DisplayName("actualizarMesa: estado inválido → 400")
    void actualizarMesaEstadoInvalido() {
        when(mesaExamenRepository.findById(3)).thenReturn(Optional.of(mesa));
        MesaExamenUpdateDTO r = new MesaExamenUpdateDTO();
        r.setEstado("PAUSADA");
        assertThrows(BusinessRuleException.class, () -> service.actualizar(3, r));
    }

    @Test
    @DisplayName("actualizarMesa: inexistente → 404")
    void actualizarMesaInexistente() {
        when(mesaExamenRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.actualizar(99, new MesaExamenUpdateDTO()));
    }

    // ------------------------------------------------------------------
    // Inscripciones (ALUMNO)
    // ------------------------------------------------------------------

    private void stubInscribir() {
        when(mesaExamenRepository.findById(3)).thenReturn(Optional.of(mesa));
        when(inscripcionMesaRepository.existsByMesaIdAndAlumnoIdAndActivaTrue(3, 7))
                .thenReturn(false);
        when(inscripcionMesaRepository.save(any(InscripcionMesa.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("inscribir: caso feliz crea inscripción activa y calcula cupos")
    void inscribir() {
        stubInscribir();
        when(inscripcionMesaRepository.findByMesaIdAndActivaTrue(3))
                .thenReturn(List.of(inscripcionMesa(50, true)));

        MesaExamenDTO dto = service.inscribir(alumnoSesion, 3);

        assertEquals(1, dto.getInscriptos());
        assertEquals(29, dto.getCuposDisponibles());
        verify(inscripcionMesaRepository).save(any(InscripcionMesa.class));
    }

    @Test
    @DisplayName("inscribir: duplicada → 409")
    void inscribirDuplicada() {
        stubInscribir();
        when(inscripcionMesaRepository.existsByMesaIdAndAlumnoIdAndActivaTrue(3, 7))
                .thenReturn(true);
        assertThrows(ConflictException.class, () -> service.inscribir(alumnoSesion, 3));
    }

    @Test
    @DisplayName("inscribir: mesa llena → 409")
    void inscribirMesaLlena() {
        stubInscribir();
        when(inscripcionMesaRepository.countByMesaIdAndActivaTrue(3)).thenReturn(30L);
        assertThrows(ConflictException.class, () -> service.inscribir(alumnoSesion, 3));
    }

    @Test
    @DisplayName("inscribir: mesa pasada → 400")
    void inscribirMesaPasada() {
        stubInscribir();
        mesa.setFecha(LocalDate.now().minusDays(2));
        assertThrows(BusinessRuleException.class, () -> service.inscribir(alumnoSesion, 3));
    }

    @Test
    @DisplayName("inscribir: mesa cerrada → 409")
    void inscribirMesaCerrada() {
        stubInscribir();
        mesa.setEstado("CERRADA");
        assertThrows(ConflictException.class, () -> service.inscribir(alumnoSesion, 3));
    }

    @Test
    @DisplayName("inscribir: inexistente → 404")
    void inscribirInexistente() {
        when(mesaExamenRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.inscribir(alumnoSesion, 99));
    }

    // ------------------------------------------------------------------
    // Baja (RF50)
    // ------------------------------------------------------------------

    private InscripcionMesa inscripcionMesa(Integer id, boolean activa) {
        InscripcionMesa insc = new InscripcionMesa();
        insc.setId(id);
        insc.setMesa(mesa);
        insc.setAlumno(alumno);
        insc.setActiva(activa);
        return insc;
    }

    @Test
    @DisplayName("darDeBaja: baja logica libera el cupo")
    void darDeBaja() {
        when(mesaExamenRepository.findById(3)).thenReturn(Optional.of(mesa));
        InscripcionMesa insc = inscripcionMesa(50, true);
        when(inscripcionMesaRepository.findById(50)).thenReturn(Optional.of(insc));
        when(inscripcionMesaRepository.save(any(InscripcionMesa.class))).thenAnswer(inv -> inv.getArgument(0));

        MesaExamenDTO dto = service.darDeBaja(alumnoSesion, 3, 50);

        assertFalse(insc.getActiva());
        assertEquals(0, dto.getInscriptos());
        verify(inscripcionMesaRepository).save(insc);
    }

    @Test
    @DisplayName("darDeBaja: inscripción de otro alumno → 403")
    void darDeBajaDeOtroAlumno() {
        Alumno otro = new Alumno();
        otro.setId(77);
        InscripcionMesa inscOtros = new InscripcionMesa();
        inscOtros.setId(51);
        inscOtros.setMesa(mesa);
        inscOtros.setAlumno(otro);
        inscOtros.setActiva(true);
        when(mesaExamenRepository.findById(3)).thenReturn(Optional.of(mesa));
        when(inscripcionMesaRepository.findById(51)).thenReturn(Optional.of(inscOtros));

        assertThrows(AccessDeniedException.class,
                () -> service.darDeBaja(alumnoSesion, 3, 51));
        verify(inscripcionMesaRepository, never()).save(any(InscripcionMesa.class));
    }

    // ------------------------------------------------------------------
    // Listado
    // ------------------------------------------------------------------

    @Test
    @DisplayName("listar: devuelve mesas con cupos e inscriptos calculados")
    void listar() {
        when(mesaExamenRepository.findAll()).thenReturn(List.of(mesa));
        InscripcionMesa i = inscripcionMesa(50, true);
        when(inscripcionMesaRepository.findByMesaIdAndActivaTrue(3)).thenReturn(List.of(i));

        List<MesaExamenDTO> lista = service.listar();

        assertEquals(1, lista.size());
        assertEquals(1, lista.get(0).getInscriptos());
        assertEquals(29, lista.get(0).getCuposDisponibles());
        assertEquals(1, lista.get(0).getAlumnosInscriptosIds().size());
    }
}
