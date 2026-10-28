package com.academit.education.service.impl;

import com.academit.education.dto.ciclo.CicloLectivoCreateDTO;
import com.academit.education.dto.ciclo.CicloLectivoDTO;
import com.academit.education.dto.ciclo.CicloLectivoUpdateDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.CicloLectivo;
import com.academit.education.model.enums.EstadoCicloEnum;
import com.academit.education.repository.CicloLectivoRepository;
import com.academit.education.service.CicloLectivoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CicloLectivoServiceImpl implements CicloLectivoService {

    private final CicloLectivoRepository cicloLectivoRepository;

    public CicloLectivoServiceImpl(CicloLectivoRepository cicloLectivoRepository) {
        this.cicloLectivoRepository = cicloLectivoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CicloLectivoDTO> listarCiclos() {
        return cicloLectivoRepository.findAll().stream()
                .sorted(Comparator.comparing(CicloLectivo::getAnio).reversed())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CicloLectivoDTO crearCiclo(CicloLectivoCreateDTO request) {
        validarFechas(request.getFechaInicio(), request.getFechaFin());

        if (cicloLectivoRepository.existsByAnio(request.getAnio())) {
            throw new BusinessRuleException(
                    "Ya existe un ciclo lectivo para el año " + request.getAnio());
        }

        CicloLectivo ciclo = new CicloLectivo();
        ciclo.setAnio(request.getAnio());
        ciclo.setNombre(request.getNombre().trim());
        // Un ciclo recién registrado nace PLANIFICADO si no se indica estado
        ciclo.setEstado(validarEstado(request.getEstado() != null
                ? request.getEstado() : EstadoCicloEnum.PLANIFICADO.name()));
        ciclo.setFechaInicio(request.getFechaInicio());
        ciclo.setFechaFin(request.getFechaFin());

        return toDto(cicloLectivoRepository.save(ciclo));
    }

    @Override
    @Transactional
    public CicloLectivoDTO actualizarCiclo(Integer id, CicloLectivoUpdateDTO request) {
        CicloLectivo ciclo = cicloLectivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ciclo lectivo no encontrado: " + id));

        // Actualización parcial: solo se cambian los campos enviados
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            ciclo.setNombre(request.getNombre().trim());
        }
        if (request.getFechaInicio() != null) ciclo.setFechaInicio(request.getFechaInicio());
        if (request.getFechaFin() != null) ciclo.setFechaFin(request.getFechaFin());
        if (request.getEstado() != null) {
            ciclo.setEstado(validarEstado(request.getEstado()));
        }

        validarFechas(ciclo.getFechaInicio(), ciclo.getFechaFin());
        return toDto(cicloLectivoRepository.save(ciclo));
    }

    /**
     * Regla del esquema: fecha_fin > fecha_inicio. En el service para dar un
     * mensaje legible antes de llegar a la restricción de la base.
     */
    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio != null && fechaFin != null && !fechaFin.isAfter(fechaInicio)) {
            throw new BusinessRuleException(
                    "La fecha de fin debe ser posterior a la fecha de inicio");
        }
    }

    /**
     * Valida que el estado pertenezca al catálogo del contrato
     * (ACTIVO, CERRADO, PLANIFICADO) y lo devuelve normalizado.
     */
    private String validarEstado(String estado) {
        try {
            return EstadoCicloEnum.valueOf(estado.trim()).name();
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessRuleException(
                    "Estado de ciclo inválido: " + estado);
        }
    }

    private CicloLectivoDTO toDto(CicloLectivo ciclo) {
        CicloLectivoDTO dto = new CicloLectivoDTO();
        dto.setIdCiclo(ciclo.getId());
        dto.setAnio(ciclo.getAnio());
        dto.setNombre(ciclo.getNombre());
        dto.setFechaInicio(ciclo.getFechaInicio());
        dto.setFechaFin(ciclo.getFechaFin());
        dto.setEstado(ciclo.getEstado());
        return dto;
    }
}
