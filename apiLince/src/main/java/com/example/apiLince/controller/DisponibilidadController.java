package com.example.apiLince.controller;

import com.example.apiLince.dto.request.BloqueoRequestDTO;
import com.example.apiLince.model.BloqueoCalendario;
import com.example.apiLince.model.UsuarioLocal;
import com.example.apiLince.repository.UsuarioLocalRepository;
import com.example.apiLince.service.DisponibilidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilidad")
@Tag(name = "Disponibilidad y Calendario", description = "Endpoints para que transportistas y guías bloqueen días libres")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;
    private final UsuarioLocalRepository usuarioLocalRepository;

    public DisponibilidadController(DisponibilidadService disponibilidadService, UsuarioLocalRepository usuarioLocalRepository) {
        this.disponibilidadService = disponibilidadService;
        this.usuarioLocalRepository = usuarioLocalRepository;
    }

    @PostMapping("/bloquear")
    @Operation(summary = "Bloquear fecha", description = "Bloquea un día en el calendario verificando la regla de 7 días de anticipación")
    public ResponseEntity<BloqueoCalendario> bloquearFecha(@Valid @RequestBody BloqueoRequestDTO request, Principal principal) {
        UsuarioLocal usuario = usuarioLocalRepository.findByCorreo(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        BloqueoCalendario bloqueo = disponibilidadService.bloquearFecha(request, usuario.getId());
        return new ResponseEntity<>(bloqueo, HttpStatus.CREATED);
    }

    @GetMapping("/mis-bloqueos")
    @Operation(summary = "Ver mis fechas bloqueadas", description = "Retorna todos los bloqueos del usuario autenticado")
    public ResponseEntity<List<BloqueoCalendario>> obtenerMisBloqueos(Principal principal) {
        UsuarioLocal usuario = usuarioLocalRepository.findByCorreo(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<BloqueoCalendario> lista = disponibilidadService.obtenerBloqueosUsuario(usuario.getId());
        return ResponseEntity.ok(lista);
    }
}