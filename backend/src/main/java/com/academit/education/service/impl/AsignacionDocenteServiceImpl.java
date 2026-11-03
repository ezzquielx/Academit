package com.academit.education.service.impl;

import com.academit.education.model.CursoMateria;
import com.academit.education.model.Usuario;
import com.academit.education.repository.CursoMateriaRepository;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.AsignacionDocenteService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AsignacionDocenteServiceImpl implements AsignacionDocenteService {

    private final CursoMateriaRepository cursoMateriaRepository;

    public AsignacionDocenteServiceImpl(CursoMateriaRepository cursoMateriaRepository) {
        this.cursoMateriaRepository = cursoMateriaRepository;
    }

    @Override
    public Integer idDocente(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        Usuario usuario = userDetails.getUsuario();
        return usuario != null && usuario.getDocente() != null ? usuario.getDocente().getId() : null;
    }

    @Override
    public boolean esAdministrador(CustomUserDetails userDetails) {
        return userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }

    @Override
    public boolean estaAsignado(Integer idDocente, Integer idCurso, Integer idMateria) {
        return idDocente != null
                && cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(idCurso, idMateria, idDocente);
    }

    @Override
    public void verificarAsignado(CustomUserDetails userDetails, Integer idCurso, Integer idMateria) {
        if (esAdministrador(userDetails)) {
            return;
        }
        if (!estaAsignado(idDocente(userDetails), idCurso, idMateria)) {
            throw new AccessDeniedException(
                    "Solo el docente asignado a la cátedra puede registrar notas o asistencia (RN11)");
        }
    }

    @Override
    public List<Integer> catedrasDelDocente(CustomUserDetails userDetails) {
        Integer idDocente = idDocente(userDetails);
        if (idDocente == null) {
            return List.of();
        }
        return cursoMateriaRepository.findByDocenteId(idDocente).stream()
                .filter(cm -> cm.getCurso() != null && cm.getMateria() != null)
                .map(CursoMateria::getId)
                .distinct()
                .toList();
    }
}
