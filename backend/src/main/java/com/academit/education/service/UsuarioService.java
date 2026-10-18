package com.academit.education.service;

import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.dto.usuario.PerfilUsuarioDTO;
import com.academit.education.dto.usuario.UsuarioRequestDTO;
import com.academit.education.dto.usuario.UsuarioResponseDTO;

import java.util.List;

public interface UsuarioService {

    PerfilUsuarioDTO getPerfil(String username);

    PerfilUsuarioDTO actualizarPerfil(String username, PerfilUsuarioDTO request);

    List<UsuarioResponseDTO> listarUsuarios(String rol, String estado);

    UsuarioResponseDTO obtenerPorId(Integer id);

    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request);

    UsuarioResponseDTO actualizarUsuario(Integer id, UsuarioRequestDTO request);

    ApiResponseDTO darDeBaja(Integer id);
}

