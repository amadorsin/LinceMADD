package com.example.apiLince.controller;

import com.example.apiLince.dto.request.ConfirmacionRequestDTO;
import com.example.apiLince.model.ConfirmacionServicio;
import com.example.apiLince.model.UsuarioLocal;
import com.example.apiLince.repository.UsuarioLocalRepository;
import com.example.apiLince.service.ConfirmacionServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/confirmaciones")
@Tag(name = "Confirmaciones de Servicios", description = "Endpoints para registrar aceptación o rechazo de servicios asignados")
public class ConfirmacionServicioController {

    private final ConfirmacionServicioService confirmacionService;
    private final UsuarioLocalRepository usuarioLocalRepository;

    public ConfirmacionServicioController(ConfirmacionServicioService confirmacionService,
                                          UsuarioLocalRepository usuarioLocalRepository) {
        this.confirmacionService = confirmacionService;
        this.usuarioLocalRepository = usuarioLocalRepository;
    }

    @PostMapping("/registrar")
    @Operation(summary = "Confirmar o rechazar servicio", description = "Persiste la confirmación o rechazo del usuario autenticado para un servicio")
    public ResponseEntity<ConfirmacionServicio> registrar(
            @Valid @RequestBody ConfirmacionRequestDTO request,
            Principal principal) {
        UsuarioLocal usuario = usuarioLocalRepository.findByCorreo(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        ConfirmacionServicio guardada = confirmacionService.registrarConfirmacion(request, usuario.getId());
        return new ResponseEntity<>(guardada, HttpStatus.CREATED);
    }

    @GetMapping("/servicio/{servicioId}")
    @Operation(summary = "Ver confirmaciones por servicio", description = "Retorna las confirmaciones/rechazos registrados para un servicio")
    public ResponseEntity<List<ConfirmacionServicio>> obtenerPorServicio(@PathVariable String servicioId) {
        return ResponseEntity.ok(confirmacionService.obtenerPorServicio(servicioId));
    }

    @GetMapping("/mias")
    @Operation(summary = "Ver mis confirmaciones", description = "Retorna las confirmaciones/rechazos del usuario autenticado")
    public ResponseEntity<List<ConfirmacionServicio>> obtenerMias(Principal principal) {
        UsuarioLocal usuario = usuarioLocalRepository.findByCorreo(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return ResponseEntity.ok(confirmacionService.obtenerPorUsuario(usuario.getId()));
    }
}