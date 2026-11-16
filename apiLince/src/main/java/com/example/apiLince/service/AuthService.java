package com.example.apiLince.service;

import com.example.apiLince.dto.request.LoginRequestDTO;
import com.example.apiLince.dto.response.AuthResponseDTO;
import com.example.apiLince.model.UsuarioLocal;
import com.example.apiLince.repository.UsuarioLocalRepository;
import com.example.apiLince.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioLocalRepository usuarioLocalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioLocalRepository usuarioLocalRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.usuarioLocalRepository = usuarioLocalRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponseDTO autenticar(LoginRequestDTO request) {
        // 1. Buscar usuario por correo (evitamos revelar si falla el correo o la clave)
        UsuarioLocal usuario = usuarioLocalRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        // 2. Validar contraseña encriptada
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        // 3. Generar token JWT
        String token = jwtTokenProvider.generarToken(usuario);

        // 4. Retornar el DTO formateado para Retrofit en Android
        return new AuthResponseDTO(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getRol().name()
        );
    }
}