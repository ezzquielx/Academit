package com.academit.education.service.impl;

import com.academit.education.dto.materia.MateriaCreateDTO;
import com.academit.education.dto.materia.MateriaDTO;
import com.academit.education.dto.materia.MateriaUpdateDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Correlativa;
import com.academit.education.model.CorrelativaId;
import com.academit.education.model.Materia;
import com.academit.education.model.PlanEstudio;
import com.academit.education.model.PlanMateria;
import com.academit.education.model.PlanMateriaId;
import com.academit.education.model.enums.RegimenMateriaEnum;
import com.academit.education.repository.CorrelativaRepository;
import com.academit.education.repository.MateriaRepository;
import com.academit.education.repository.PlanEstudioRepository;
import com.academit.education.repository.PlanMateriaRepository;
import com.academit.education.service.MateriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MateriaServiceImpl implements MateriaService {

    private final MateriaRepository materiaRepository;
    private final PlanEstudioRepository planEstudioRepository;
    private final PlanMateriaRepository planMateriaRepository;
    private final CorrelativaRepository correlativaRepository;

    public MateriaServiceImpl(
            MateriaRepository materiaRepository,
            PlanEstudioRepository planEstudioRepository,
            PlanMateriaRepository planMateriaRepository,
            CorrelativaRepository correlativaRepository) {
        this.materiaRepository = materiaRepository;
        this.planEstudioRepository = planEstudioRepository;
        this.planMateriaRepository = planMateriaRepository;
        this.correlativaRepository = correlativaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MateriaDTO> listarMaterias(Integer idCarrera, Integer idPlan) {
        List<PlanMateria> relaciones;
        if (idPlan != null) {
            relaciones = planMateriaRepository.findByPlanId(idPlan);
        } else if (idCarrera != null) {
            relaciones = planMateriaRepository.findByPlanCarreraId(idCarrera);
        } else {
            relaciones = planMateriaRepository.findAll();
        }
        return relaciones.stream()
                .sorted(Comparator.comparing(PlanMateria::getAnioPlan)
                        .thenComparing(pm -> pm.getMateria().getNombre()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MateriaDTO crearMateria(MateriaCreateDTO request) {
        PlanEstudio plan = planEstudioRepository.findById(request.getIdPlan())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan de estudios no encontrado con ID: " + request.getIdPlan()));

        if (!plan.getCarrera().getId().equals(request.getIdCarrera())) {
            throw new BusinessRuleException(
                    "El plan de estudios indicado no pertenece a la carrera seleccionada (RN22)");
        }

        String nombre = request.getNombre().trim();
        if (materiaRepository.existsByNombre(nombre)) {
            throw new BusinessRuleException(
                    "Ya existe una materia con el nombre " + nombre);
        }

        validarRegimen(request.getRegimen());

        Materia materia = new Materia();
        materia.setCodigo(request.getCodigo().trim());
        materia.setNombre(nombre);
        materia.setRegimen(RegimenMateriaEnum.valueOf(request.getRegimen().trim()).name());
        materia.setHorasSemanales(request.getHorasSemanales());
        materiaRepository.save(materia);

        PlanMateria pm = new PlanMateria();
        pm.setPlan(plan);
        pm.setMateria(materia);
        pm.setAnioPlan(request.getAnio());
        planMateriaRepository.save(pm);

        List<Integer> correlativas = request.getCorrelativas() != null
                ? request.getCorrelativas() : List.of();
        guardarCorrelativas(plan.getId(), materia.getId(), correlativas);

        return toDto(pm);
    }

    @Override
    @Transactional
    public MateriaDTO actualizarMateria(Integer idMateria, MateriaUpdateDTO request) {
        Materia materia = materiaRepository.findById(idMateria)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Materia no encontrada con ID: " + idMateria));

        if (request.getNombre() != null && !request.getNombre().isBlank()
                && !request.getNombre().trim().equalsIgnoreCase(materia.getNombre())) {
            String nombre = request.getNombre().trim();
            if (materiaRepository.existsByNombreAndIdNot(nombre, idMateria)) {
                throw new BusinessRuleException(
                        "Ya existe una materia con el nombre " + nombre);
            }
            materia.setNombre(nombre);
        }
        if (request.getRegimen() != null) {
            materia.setRegimen(validarRegimen(request.getRegimen()));
        }
        if (request.getHorasSemanales() != null) {
            materia.setHorasSemanales(request.getHorasSemanales());
        }
        materiaRepository.save(materia);

        // El año y las correlativas pertenecen al plan: solo se aplican
        // if the materia is linked to a single plan (decision documented).
        List<PlanMateria> relaciones = planMateriaRepository.findByMateriaId(idMateria);
        if ((request.getAnio() != null || request.getCorrelativas() != null)
                && relaciones.size() != 1) {
            throw new BusinessRuleException(
                    "La materia pertenece a " + relaciones.size()
                            + " planes de estudio: no se puede actualizar el año ni las "
                            + "correlatividades sin indicar el plan");
        }
        if (!relaciones.isEmpty()) {
            PlanMateria pm = relaciones.get(0);
            if (request.getAnio() != null) {
                pm.setAnioPlan(request.getAnio());
            }
            planMateriaRepository.save(pm);

            if (request.getCorrelativas() != null) {
                Integer idPlan = pm.getId().getIdPlan();
                if (request.getCorrelativas().contains(idMateria)) {
                    throw new BusinessRuleException(
                            "Una materia no puede ser correlativa de sí misma (RN24)");
                }
                // Reemplazo completo del set de correlativas
                correlativaRepository.findByIdIdPlanAndIdIdMateria(idPlan, idMateria)
                        .forEach(correlativaRepository::delete);
                guardarCorrelativas(idPlan, idMateria, request.getCorrelativas());
            }
        }

        // Reload for the response
        PlanMateria pm = relaciones.isEmpty()
                ? planMateriaSinPlan(materia) : relaciones.get(0);
        return toDto(pm);
    }

    // ------------------------------------------------------------------

    /**
     * Acorde a RN24: las correlativas solo vinculan materias del mismo plan,
     * no pueden apuntar a la misma materia ni formar un ciclo.
     */
    private void guardarCorrelativas(Integer idPlan, Integer idMateria,
                                     List<Integer> correlativas) {
        if (correlativas.isEmpty()) {
            return;
        }

        List<Correlativa> existentes = correlativaRepository.findByIdIdPlan(idPlan);
        Map<Integer, List<Integer>> grafo = new HashMap<>();
        for (Correlativa c : existentes) {
            grafo.computeIfAbsent(c.getId().getIdMateria(), k -> new ArrayList<>())
                    .add(c.getId().getIdMateriaRequerida());
        }

        for (Integer requerida : correlativas) {
            if (requerida.equals(idMateria)) {
                throw new BusinessRuleException(
                        "Una materia no puede ser correlativa de sí misma (RN24)");
            }
            if (!planMateriaRepository.existsByIdIdPlanAndIdIdMateria(idPlan, requerida)) {
                throw new BusinessRuleException(
                        "La materia correlativa " + requerida
                                + " no pertenece al plan de estudios (RN24)");
            }
            if (formaCiclo(grafo, requerida, idMateria)) {
                throw new BusinessRuleException(
                        "La correlatividad " + idMateria + " -> " + requerida
                                + " forma un ciclo de correlativas (RN24)");
            }
            Correlativa c = new Correlativa();
            CorrelativaId cid = new CorrelativaId();
            cid.setIdPlan(idPlan);
            cid.setIdMateria(idMateria);
            cid.setIdMateriaRequerida(requerida);
            c.setId(cid);
            correlativaRepository.save(c);
            grafo.computeIfAbsent(idMateria, k -> new ArrayList<>()).add(requerida);
        }
    }

    /**
     * Devuelve true si la materia requerida alcanza (directa o
     * transitivamente) a la materia del nuevo vínculo: el nuevo arco
     * cerraría un ciclo (RN24).
     */
    private boolean formaCiclo(Map<Integer, List<Integer>> grafo,
                               Integer desde, Integer objetivo) {
        List<Integer> pendientes = new ArrayList<>();
        pendientes.add(desde);
        List<Integer> visitados = new ArrayList<>();
        while (!pendientes.isEmpty()) {
            Integer actual = pendientes.remove(pendientes.size() - 1);
            if (actual.equals(objetivo)) {
                return true;
            }
            if (visitados.contains(actual)) {
                continue;
            }
            visitados.add(actual);
            pendientes.addAll(grafo.getOrDefault(actual, List.of()));
        }
        return false;
    }

    private String validarRegimen(String regimen) {
        try {
            return RegimenMateriaEnum.valueOf(regimen.trim()).name();
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessRuleException(
                    "Régimen de materia inválido: " + regimen
                            + " (valores permitidos: ANUAL, CUATRIMESTRAL)");
        }
    }

    /** Respuesta cuando la materia todavía no está en ningún plan. */
    private PlanMateria planMateriaSinPlan(Materia materia) {
        PlanMateria pm = new PlanMateria();
        PlanMateriaId pid = new PlanMateriaId();
        pid.setIdMateria(materia.getId());
        pm.setId(pid);
        pm.setMateria(materia);
        return pm;
    }

    private MateriaDTO toDto(PlanMateria pm) {
        MateriaDTO dto = new MateriaDTO();
        Materia materia = pm.getMateria();
        dto.setIdMateria(materia.getId());
        dto.setCodigo(materia.getCodigo());
        dto.setNombre(materia.getNombre());
        dto.setIdCarrera(pm.getPlan() != null ? pm.getPlan().getCarrera().getId() : null);
        dto.setCarreraNombre(pm.getPlan() != null ? pm.getPlan().getCarrera().getNombre() : null);
        dto.setIdPlan(pm.getPlan() != null ? pm.getPlan().getId() : null);
        dto.setPlanNombre(pm.getPlan() != null ? pm.getPlan().getNombre() : null);
        dto.setAnio(pm.getAnioPlan());
        dto.setRegimen(materia.getRegimen());
        dto.setHorasSemanales(materia.getHorasSemanales());
        List<Integer> correlativasIds = List.of();
        if (pm.getPlan() != null && pm.getId() != null && pm.getId().getIdPlan() != null) {
            correlativasIds = correlativaRepository
                    .findByIdIdPlanAndIdIdMateria(pm.getId().getIdPlan(), materia.getId()).stream()
                    .map(c -> c.getId().getIdMateriaRequerida())
                    .collect(Collectors.toList());
        }
        dto.setCorrelativas(correlativasIds);
        return dto;
    }
}
