package com.academit.education.service;

import com.academit.education.dto.curso.CursoCreateDTO;
import com.academit.education.dto.curso.CursoDTO;
import com.academit.education.dto.curso.CursoUpdateDTO;

import java.util.List;

public interface CursoService {

    List<CursoDTO> listarCursos();

    CursoDTO crearCurso(CursoCreateDTO request);

    CursoDTO actualizarCurso(Integer id, CursoUpdateDTO request);
}
