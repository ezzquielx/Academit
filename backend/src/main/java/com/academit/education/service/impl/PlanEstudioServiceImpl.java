package com.academit.education.service.impl;

import com.academit.education.dto.materia.MateriaDTO;
import com.academit.education.dto.planestudio.PlanEstudioDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.PlanEstudio;
import com.academit.education.model.PlanMateria;
import com.academit.education.model.enums.EstadoPlanEstudioEnum;
import com.academit.education.repository.CorrelativaRepository;
import com.academit.education.repository.PlanEstudioRepository;
import com.academit.education.repository.PlanMateriaRepository;
import com.academit.education.service.PlanEstudioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlanEstudioServiceImpl implements PlanEstudioService {

    private final PlanEstudioRepository planEstudioRepository;
    private final PlanMateriaRepository planMateriaRepository;
    private final CorrelativaRepository correlativaRepository;

    public PlanEstudioServiceImpl(
            PlanEstudioRepository planEstudioRepository,
            PlanMateriaRepository planMateriaRepository,
            CorrelativaRepository correlativaRepository) {
        this.planEstudioRepository = planEstudioRepository;
        this.planMateriaRepository = planMateriaRepository;
        this.correlativaRepository = correlativaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanEstudioDTO> listarPlanes() {
        return planEstudioRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MateriaDTO> listarMateriasDePlan(Integer idPlan) {
        PlanEstudio plan = obtenerPlan(idPlan);

        // Correlativas del plan agrupadas por materia (una sola consulta)
        Map<Integer, List<Integer>> correlativasPorMateria = correlativaRepository
                .findByIdIdPlan(idPlan).stream()
                .collect(Collectors.groupingBy(
                        c -> c.getId().getIdMateria(),
                        Collectors.mapping(c -> c.getId().getIdMateriaRequerida(),
                                Collectors.toList())));

        return planMateriaRepository.findByPlanId(idPlan).stream()
                .sorted(Comparator.comparing(PlanMateria::getAnioPlan)
                        .thenComparing(pm -> pm.getMateria().getNombre()))
                .map(pm -> toMateriaDto(pm, plan.getId(), correlativasPorMateria))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------

    private PlanEstudio obtenerPlan(Integer idPlan) {
        return planEstudioRepository.findById(idPlan)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan de estudios no encontrado con ID: " + idPlan));
    }

    private PlanEstudioDTO toDto(PlanEstudio plan) {
        PlanEstudioDTO dto = new PlanEstudioDTO();
        dto.setIdPlan(plan.getId());
        dto.setIdCarrera(plan.getCarrera().getId());
        dto.setCarreraNombre(plan.getCarrera().getNombre());
        dto.setCodigo(plan.getCodigo());
        dto.setNombre(plan.getNombre());
        dto.setAnioAprobacion(plan.getAnioAprobacion());
        // Campo calculado: materias del plan, nunca persistido
        dto.setCantidadMaterias(planMateriaRepository.findByPlanId(plan.getId()).size());
        dto.setEstado(plan.getEstado() != null
                ? plan.getEstado() : EstadoPlanEstudioEnum.VIGENTE.name());
        return dto;
    }

    private MateriaDTO toMateriaDto(PlanMateria pm, Integer idPlan,
                                    Map<Integer, List<Integer>> correlativasPorMateria) {
        MateriaDTO dto = new MateriaDTO();
        dto.setIdMateria(pm.getMateria().getId());
        dto.setCodigo(pm.getMateria().getCodigo());
        dto.setNombre(pm.getMateria().getNombre());
        dto.setIdCarrera(pm.getPlan().getCarrera().getId());
        dto.setCarreraNombre(pm.getPlan().getCarrera().getNombre());
        dto.setIdPlan(pm.getPlan().getId());
        dto.setPlanNombre(pm.getPlan().getNombre());
        dto.setAnio(pm.getAnioPlan());
        dto.setRegimen(pm.getMateria().getRegimen());
        dto.setHorasSemanales(pm.getMateria().getHorasSemanales());
        List<Integer> correlativas = correlativasPorMateria
                .getOrDefault(pm.getMateria().getId(), List.of());
        dto.setCorrelativas(correlativas);
        return dto;
    }
}
