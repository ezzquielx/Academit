package com.academit.education.service;

import com.academit.education.dto.auth.AuthMeResponseDTO;
import com.academit.education.dto.auth.CambiarPasswordDTO;
import com.academit.education.dto.auth.LoginRequestDTO;
import com.academit.education.dto.auth.LoginResponseDTO;
import com.academit.education.dto.auth.RegistroAlumnoDTO;
import com.academit.education.dto.common.ApiResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO request);

    ApiResponseDTO registrarAlumno(RegistroAlumnoDTO request);

    AuthMeResponseDTO getMe(String username);

    void logout();

    ApiResponseDTO cambiarPassword(String username, CambiarPasswordDTO request);
}
