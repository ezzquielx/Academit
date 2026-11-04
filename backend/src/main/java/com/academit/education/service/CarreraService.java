package com.academit.education.service;

import com.academit.education.dto.carrera.CarreraCreateDTO;
import com.academit.education.dto.carrera.CarreraDTO;
import com.academit.education.dto.carrera.CarreraUpdateDTO;
import com.academit.education.dto.curso.CursoDTO;
import com.academit.education.dto.planestudio.PlanEstudioDTO;

import java.util.List;

public interface CarreraService {

    List<CarreraDTO> listarCarreras();

    CarreraDTO obtenerPorId(Integer id);

    CarreraDTO crearCarrera(CarreraCreateDTO request);

    CarreraDTO actualizarCarrera(Integer id, CarreraUpdateDTO request);

    /** DELETE del contrato: baja lógica (estado INACTIVA), no borra la fila. */
    void darDeBajaCarrera(Integer id);

    /** GET /api/carreras/{id}/planes: planes de esa carrera. */
    List<PlanEstudioDTO> listarPlanesDeCarrera(Integer idCarrera);

    /** GET /api/carreras/{id}/cursos: cursos de esa carrera. */
    List<CursoDTO> listarCursosDeCarrera(Integer idCarrera);
}
