package com.academit.education.service;

import com.academit.education.dto.materia.MateriaCreateDTO;
import com.academit.education.dto.materia.MateriaDTO;
import com.academit.education.dto.materia.MateriaUpdateDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Carrera;
import com.academit.education.model.Correlativa;
import com.academit.education.model.CorrelativaId;
import com.academit.education.model.Materia;
import com.academit.education.model.PlanEstudio;
import com.academit.education.model.PlanMateria;
import com.academit.education.model.PlanMateriaId;
import com.academit.education.repository.CorrelativaRepository;
import com.academit.education.repository.MateriaRepository;
import com.academit.education.repository.PlanEstudioRepository;
import com.academit.education.repository.PlanMateriaRepository;
import com.academit.education.service.impl.MateriaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MateriaServiceImplTest {

    @Mock
    private MateriaRepository materiaRepository;

    @Mock
    private PlanEstudioRepository planEstudioRepository;

    @Mock
    private PlanMateriaRepository planMateriaRepository;

    @Mock
    private CorrelativaRepository correlativaRepository;

    @InjectMocks
    private MateriaServiceImpl service;

    private Carrera carrera;
    private PlanEstudio plan;
    private Materia prog;
    private Materia corr;
    private PlanMateria relacion;

    @BeforeEach
    void setUp() {
        carrera = new Carrera();
        carrera.setId(1);
        carrera.setCodigo("TUP");
        carrera.setNombre("Tecnicatura Universitaria en Programación");

        plan = new PlanEstudio();
        plan.setId(5);
        plan.setCarrera(carrera);
        plan.setCodigo("PLAN-2024-TUP");
        plan.setNombre("Plan 2024");
        plan.setAnioAprobacion(2024);
        plan.setEstado("VIGENTE");

        prog = new Materia();
        prog.setId(10);
        prog.setCodigo("PROG-II");
        prog.setNombre("Programación II");
        prog.setRegimen("ANUAL");
        prog.setHorasSemanales(5);

        corr = new Materia();
        corr.setId(9);
        corr.setCodigo("PROG-I");
        corr.setNombre("Programación I");
        corr.setRegimen("ANUAL");

        PlanMateriaId pid = new PlanMateriaId();
        pid.setIdPlan(5);
        pid.setIdMateria(10);
        relacion = new PlanMateria();
        relacion.setId(pid);
        relacion.setPlan(plan);
        relacion.setMateria(prog);
        relacion.setAnioPlan(2);
    }

    private MateriaCreateDTO createDTO(List<Integer> correlativas) {
        MateriaCreateDTO r = new MateriaCreateDTO();
        r.setCodigo("MAT-I");
        r.setNombre("Matemática");
        r.setIdCarrera(1);
        r.setIdPlan(5);
        r.setAnio(1);
        r.setRegimen("CUATRIMESTRAL");
        r.setHorasSemanales(4);
        r.setCorrelativas(correlativas);
        return r;
    }

    @Test
    @DisplayName("crearMateria: caso feliz con correlativa del mismo plan")
    void crearMateria() {
        when(planEstudioRepository.findById(5)).thenReturn(Optional.of(plan));
        when(planMateriaRepository.existsByIdIdPlanAndIdIdMateria(5, 9)).thenReturn(true);
        when(correlativaRepository.findByIdIdPlan(5)).thenReturn(List.of());
        when(materiaRepository.save(any(Materia.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planMateriaRepository.save(any(PlanMateria.class)))
                .thenAnswer(inv -> {
                    PlanMateria pm = inv.getArgument(0);
                    PlanMateriaId pid = new PlanMateriaId();
                    pid.setIdPlan(5);
                    pid.setIdMateria(pm.getMateria().getId());
                    pm.setId(pid);
                    return pm;
                });

        MateriaDTO dto = service.crearMateria(createDTO(List.of(9)));

        assertEquals("Matemática", dto.getNombre());
        assertEquals(1, dto.getAnio());
        verify(correlativaRepository).save(any(Correlativa.class));
    }

    @Test
    @DisplayName("crearMateria: rechaza nombre repetido")
    void crearMateriaNombreRepetido() {
        when(planEstudioRepository.findById(5)).thenReturn(Optional.of(plan));
        when(materiaRepository.existsByNombre("Programación II")).thenReturn(true);

        MateriaCreateDTO r = createDTO(List.of(9));
        r.setNombre("Programación II");

        assertThrows(BusinessRuleException.class, () -> service.crearMateria(r));
        verify(materiaRepository, never()).save(any(Materia.class));
    }

    @Test
    @DisplayName("crearMateria: rechaza plan que no pertenece a la carrera (RN22)")
    void crearMateriaPlanDeOtraCarrera() {
        when(planEstudioRepository.findById(5)).thenReturn(Optional.of(plan));

        MateriaCreateDTO r = createDTO(null);
        r.setIdCarrera(7);

        assertThrows(BusinessRuleException.class, () -> service.crearMateria(r));
        verify(materiaRepository, never()).save(any(Materia.class));
    }

    @Test
    @DisplayName("crearMateria: rechaza correlativa de otro plan (RN24)")
    void crearMateriaCorrelativaDeOtroPlan() {
        when(planEstudioRepository.findById(5)).thenReturn(Optional.of(plan));
        when(materiaRepository.save(any(Materia.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planMateriaRepository.save(any(PlanMateria.class)))
                .thenAnswer(inv -> {
                    PlanMateria pm = inv.getArgument(0);
                    PlanMateriaId pid = new PlanMateriaId();
                    pid.setIdPlan(5);
                    pid.setIdMateria(pm.getMateria().getId());
                    pm.setId(pid);
                    return pm;
                });
        when(planMateriaRepository.existsByIdIdPlanAndIdIdMateria(5, 9)).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> service.crearMateria(createDTO(List.of(9))));
    }

    @Test
    @DisplayName("crearMateria: rechaza correlativa que apunta a si misma (RN24)")
    void crearMateriaCorrelativaASiMisma() {
        when(planEstudioRepository.findById(5)).thenReturn(Optional.of(plan));
        when(materiaRepository.save(any(Materia.class))).thenAnswer(inv -> {
            Materia m = inv.getArgument(0);
            m.setId(12);
            return m;
        });
        when(planMateriaRepository.save(any(PlanMateria.class)))
                .thenAnswer(inv -> {
                    PlanMateria pm = inv.getArgument(0);
                    PlanMateriaId pid = new PlanMateriaId();
                    pid.setIdPlan(5);
                    pid.setIdMateria(pm.getMateria().getId());
                    pm.setId(pid);
                    return pm;
                });
        when(correlativaRepository.findByIdIdPlan(5)).thenReturn(List.of());

        assertThrows(BusinessRuleException.class,
                () -> service.crearMateria(createDTO(List.of(12))));
    }

    @Test
    @DisplayName("crearMateria: rechaza regimen fuera del catalogo")
    void crearMateriaRegimenInvalido() {
        when(planEstudioRepository.findById(5)).thenReturn(Optional.of(plan));

        MateriaCreateDTO r = createDTO(null);
        r.setRegimen("SEMESTRAL");

        assertThrows(BusinessRuleException.class, () -> service.crearMateria(r));
    }

    // ------------------------------------------------------------------
    // PUT
    // ------------------------------------------------------------------

    @Test
    @DisplayName("actualizarMateria: reemplaza el set de correlativas")
    void actualizarMateriaCorrelativas() {
        when(materiaRepository.findById(10)).thenReturn(Optional.of(prog));
        when(planMateriaRepository.findByMateriaId(10)).thenReturn(List.of(relacion));
        when(materiaRepository.save(any(Materia.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planMateriaRepository.save(any(PlanMateria.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(correlativaRepository.findByIdIdPlanAndIdIdMateria(5, 10))
                .thenReturn(List.of());
        when(planMateriaRepository.existsByIdIdPlanAndIdIdMateria(5, 9)).thenReturn(true);
        when(correlativaRepository.findByIdIdPlan(5)).thenReturn(List.of());

        MateriaUpdateDTO r = new MateriaUpdateDTO();
        r.setCorrelativas(List.of(9));

        MateriaDTO dto = service.actualizarMateria(10, r);

        verify(correlativaRepository).save(any(Correlativa.class));
        assertEquals(2, dto.getAnio());
    }

    @Test
    @DisplayName("actualizarMateria: rechaza correlativa que forma ciclo (RN24)")
    void actualizarMateriaCiclo() {
        when(materiaRepository.findById(10)).thenReturn(Optional.of(prog));
        when(planMateriaRepository.findByMateriaId(10)).thenReturn(List.of(relacion));
        when(materiaRepository.save(any(Materia.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planMateriaRepository.save(any(PlanMateria.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(correlativaRepository.findByIdIdPlanAndIdIdMateria(5, 10))
                .thenReturn(List.of());

        // Ya existe 9 -> 10 (Programación I como si requiriese a la nueva):
        // agregar 10 -> 9 cierra un ciclo de dos aristas (RN24).
        Correlativa a = new Correlativa();
        CorrelativaId aid = new CorrelativaId();
        aid.setIdPlan(5); aid.setIdMateria(9); aid.setIdMateriaRequerida(10);
        a.setId(aid);
        when(correlativaRepository.findByIdIdPlan(5)).thenReturn(List.of(a));
        when(planMateriaRepository.existsByIdIdPlanAndIdIdMateria(5, 9)).thenReturn(true);

        MateriaUpdateDTO r = new MateriaUpdateDTO();
        r.setCorrelativas(List.of(9));

        assertThrows(BusinessRuleException.class, () -> service.actualizarMateria(10, r));
    }

    @Test
    @DisplayName("actualizarMateria: 404 si la materia no existe")
    void actualizarMateriaInexistente() {
        when(materiaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.actualizarMateria(99, new MateriaUpdateDTO()));
    }
}
