package com.academit.education.service;

import com.academit.education.dto.alumno.AlumnoCreateDTO;
import com.academit.education.dto.alumno.AlumnoDTO;
import com.academit.education.dto.alumno.AlumnoUpdateDTO;
import com.academit.education.dto.alumno.SituacionAcademicaDTO;
import com.academit.education.dto.alumno.SituacionAdministrativaDTO;
import com.academit.education.security.CustomUserDetails;

import java.util.List;

public interface AlumnoService {

    List<AlumnoDTO> listarAlumnos();

    AlumnoDTO obtenerPorId(Integer id, CustomUserDetails userDetails);

    AlumnoDTO crearAlumno(AlumnoCreateDTO request);

    AlumnoDTO actualizarAlumno(Integer id, AlumnoUpdateDTO request);

    void eliminarAlumno(Integer id);

    SituacionAcademicaDTO obtenerSituacionAcademica(Integer id, CustomUserDetails userDetails);

    SituacionAdministrativaDTO obtenerSituacionAdministrativa(Integer id, CustomUserDetails userDetails);
}

