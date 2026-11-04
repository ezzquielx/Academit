package com.academit.education.service;

import com.academit.education.dto.documentacion.DocumentacionDTO;
import com.academit.education.dto.documentacion.DocumentacionUpdateDTO;

import java.util.List;

/**
 * Legajo documental del alumno (RF56-RF58, RN17).
 */
public interface DocumentacionService {

    /** Legajo completo; un alumno solo consulta el suyo. */
    List<DocumentacionDTO> listarDocumentacion(String username, Integer idAlumno);

    /** Actualiza estado, observaciones y fecha de entrega de un requisito. */
    DocumentacionDTO actualizarDocumentacion(Integer idDocumento, DocumentacionUpdateDTO request);
}
