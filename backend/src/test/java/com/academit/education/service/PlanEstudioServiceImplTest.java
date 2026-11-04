package com.academit.education.service;

import com.academit.education.dto.materia.MateriaDTO;
import com.academit.education.dto.planestudio.PlanEstudioDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Carrera;
import com.academit.education.model.Correlativa;
import com.academit.education.model.CorrelativaId;
import com.academit.education.model.Materia;
import com.academit.education.model.PlanEstudio;
import com.academit.education.model.PlanMateria;
import com.academit.education.model.PlanMateriaId;
import com.academit.education.repository.CorrelativaRepository;
import com.academit.education.repository.PlanEstudioRepository;
import com.academit.education.repository.PlanMateriaRepository;
import com.academit.education.service.impl.PlanEstudioServiceImpl;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanEstudioServiceImplTest {

    @Mock
    private PlanEstudioRepository planEstudioRepository;

    @Mock
    private PlanMateriaRepository planMateriaRepository;

    @Mock
    private CorrelativaRepository correlativaRepository;

    @InjectMocks
    private PlanEstudioServiceImpl service;

    private Carrera carrera;
    private PlanEstudio plan;

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
    }

    private PlanMateria relacion(Integer idMateria, String nombreMateria, Integer anio) {
        Materia materia = new Materia();
        materia.setId(idMateria);
        materia.setCodigo("CO-" + idMateria);
        materia.setNombre(nombreMateria);
        materia.setRegimen("ANUAL");
        materia.setHorasSemanales(5);

        PlanMateria pm = new PlanMateria();
        PlanMateriaId pid = new PlanMateriaId();
        pid.setIdPlan(5);
        pid.setIdMateria(idMateria);
        pm.setId(pid);
        pm.setPlan(plan);
        pm.setMateria(materia);
        pm.setAnioPlan(anio);
        return pm;
    }

    @Test
    @DisplayName("listarPlanes: devuelve planes con carreraNombre y cantidadMaterias")
    void listarPlanes() {
        when(planEstudioRepository.findAll()).thenReturn(List.of(plan));
        when(planMateriaRepository.findByPlanId(5)).thenReturn(List.of(
                relacion(10, "Programación I", 1)));

        List<PlanEstudioDTO> planes = service.listarPlanes();

        assertEquals(1, planes.size());
        assertEquals(1, planes.get(0).getCantidadMaterias());
        assertEquals("Tecnicatura Universitaria en Programación", planes.get(0).getCarreraNombre());
        assertEquals("VIGENTE", planes.get(0).getEstado());
    }

    @Test
    @DisplayName("listarMateriasDePlan: 404 si el plan no existe")
    void listarMateriasPlanInexistente() {
        when(planEstudioRepository.findById(9)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.listarMateriasDePlan(9));
    }

    @Test
    @DisplayName("listarMateriasDePlan: devuelve la malla con anio y correlativas")
    void listarMateriasDePlan() {
        when(planEstudioRepository.findById(5)).thenReturn(Optional.of(plan));
        when(planMateriaRepository.findByPlanId(5)).thenReturn(List.of(
                relacion(10, "Programación I", 1),
                relacion(11, "Programación II", 2)));

        Correlativa corr = new Correlativa();
        CorrelativaId cid = new CorrelativaId();
        cid.setIdPlan(5);
        cid.setIdMateria(11);
        cid.setIdMateriaRequerida(10);
        corr.setId(cid);
        when(correlativaRepository.findByIdIdPlan(5)).thenReturn(List.of(corr));

        List<MateriaDTO> materias = service.listarMateriasDePlan(5);

        assertEquals(2, materias.size());
        assertEquals("Programación I", materias.get(0).getNombre());
        assertEquals(1, materias.get(0).getAnio());
        assertEquals(2, materias.get(1).getAnio());
        assertEquals(List.of(10), materias.get(1).getCorrelativas());
        assertEquals("Plan 2024", materias.get(1).getPlanNombre());
    }
}
