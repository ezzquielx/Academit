package com.academit.education.service.impl;

import com.academit.education.dto.docente.DocenteCreateDTO;
import com.academit.education.dto.docente.DocenteDTO;
import com.academit.education.dto.docente.DocenteUpdateDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Docente;
import com.academit.education.model.EstadoPersona;
import com.academit.education.repository.DocenteRepository;
import com.academit.education.repository.EstadoPersonaRepository;
import com.academit.education.service.DocenteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DocenteServiceImpl implements DocenteService {

    private final DocenteRepository docenteRepository;
    private final EstadoPersonaRepository estadoPersonaRepository;

    public DocenteServiceImpl(DocenteRepository docenteRepository, EstadoPersonaRepository estadoPersonaRepository) {
        this.docenteRepository = docenteRepository;
        this.estadoPersonaRepository = estadoPersonaRepository;
    }

    @Override
    public List<DocenteDTO> listarDocentes() {
        return docenteRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public DocenteDTO obtenerPorId(Integer id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con ID: " + id));
        return toDTO(docente);
    }

    @Override
    @Transactional
    public DocenteDTO crearDocente(DocenteCreateDTO request) {
        if (docenteRepository.existsByDni(request.getDni())) {
            throw new BusinessRuleException("Ya existe un docente registrado con el DNI " + request.getDni());
        }

        if (docenteRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Ya existe un docente registrado con el correo electrónico " + request.getEmail());
        }

        EstadoPersona estadoActivo = estadoPersonaRepository.findByNombre("ACTIVO")
                .orElseGet(() -> estadoPersonaRepository.findAll().stream()
                        .filter(e -> "ACTIVO".equalsIgnoreCase(e.getNombre()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Estado de persona ACTIVO no encontrado en el sistema")));

        Docente docente = new Docente();
        docente.setNombre(request.getNombre().trim());
        docente.setApellido(request.getApellido().trim());
        docente.setDni(request.getDni().trim());
        docente.setEmail(request.getEmail().trim().toLowerCase());
        docente.setTelefono(request.getTelefono());
        docente.setTitulo(request.getTitulo() != null && !request.getTitulo().isBlank()
                ? request.getTitulo().trim()
                : "Licenciado / Profesor");
        docente.setEstado(estadoActivo);

        Docente guardado = docenteRepository.save(docente);
        return toDTO(guardado);
    }

    @Override
    @Transactional
    public DocenteDTO actualizarDocente(Integer id, DocenteUpdateDTO request) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con ID: " + id));

        if (request.getDni() != null && !request.getDni().trim().equals(docente.getDni())) {
            if (docenteRepository.existsByDniAndIdNot(request.getDni().trim(), id)) {
                throw new BusinessRuleException("Ya existe otro docente registrado con el DNI " + request.getDni());
            }
            docente.setDni(request.getDni().trim());
        }

        if (request.getEmail() != null && !request.getEmail().trim().equalsIgnoreCase(docente.getEmail())) {
            if (docenteRepository.existsByEmailAndIdNot(request.getEmail().trim(), id)) {
                throw new BusinessRuleException("Ya existe otro docente registrado con el correo electrónico " + request.getEmail());
            }
            docente.setEmail(request.getEmail().trim().toLowerCase());
        }

        if (request.getEstado() != null) {
            EstadoPersona nuevoEstado = estadoPersonaRepository.findByNombre(request.getEstado())
                    .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: " + request.getEstado()));
            docente.setEstado(nuevoEstado);
        }

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            docente.setNombre(request.getNombre().trim());
        }

        if (request.getApellido() != null && !request.getApellido().isBlank()) {
            docente.setApellido(request.getApellido().trim());
        }

        if (request.getTelefono() != null) {
            docente.setTelefono(request.getTelefono());
        }

        if (request.getTitulo() != null && !request.getTitulo().isBlank()) {
            docente.setTitulo(request.getTitulo().trim());
        }

        Docente actualizado = docenteRepository.save(docente);
        return toDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarDocente(Integer id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con ID: " + id));

        // Baja lógica: preservar integridad de asignaciones y evaluaciones históricas
        EstadoPersona estadoInactivo = estadoPersonaRepository.findByNombre("INACTIVO")
                .orElseGet(() -> estadoPersonaRepository.findAll().stream()
                        .filter(e -> "INACTIVO".equalsIgnoreCase(e.getNombre()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Estado de persona INACTIVO no configurado")));

        docente.setEstado(estadoInactivo);
        docenteRepository.save(docente);
    }

    private DocenteDTO toDTO(Docente docente) {
        DocenteDTO dto = new DocenteDTO();
        dto.setIdDocente(docente.getId());
        dto.setLegajo(String.format("DOC-%03d", 200 + docente.getId()));
        dto.setNombre(docente.getNombre());
        dto.setApellido(docente.getApellido());
        dto.setDni(docente.getDni());
        dto.setEmail(docente.getEmail());
        dto.setTelefono(docente.getTelefono());
        dto.setTitulo(docente.getTitulo());
        dto.setEstado(docente.getEstado() != null ? docente.getEstado().getNombre() : "ACTIVO");
        return dto;
    }
}

