package com.example.apiLince.controller;

import com.example.apiLince.dto.request.MarcajeRequestDTO;
import com.example.apiLince.dto.response.MensajeResponseDTO;
import com.example.apiLince.model.UsuarioLocal;
import com.example.apiLince.repository.UsuarioLocalRepository;
import com.example.apiLince.service.HitoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/hitos")
@Tag(name = "Hitos Operativos", description = "Endpoints para registrar Check-In y Check-Out geolocalizados")
public class HitoController {

    private final HitoService hitoService;
    private final UsuarioLocalRepository usuarioLocalRepository;

    public HitoController(HitoService hitoService, UsuarioLocalRepository usuarioLocalRepository) {
        this.hitoService = hitoService;
        this.usuarioLocalRepository = usuarioLocalRepository;
    }

    @PostMapping("/marcaje")
    @Operation(summary = "Registrar Marcaje", description = "Registra un check-in o check-out validando geocerca y horarios")
    public ResponseEntity<MensajeResponseDTO> registrarMarcaje(
            @Valid @RequestBody MarcajeRequestDTO request,
            @RequestParam(required = false) String puntoReferenciaPickup,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime horaProgramada,
            Principal principal) {

        // Extraemos el ID real del usuario desde su token (correo)
        UsuarioLocal usuario = usuarioLocalRepository.findByCorreo(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en la base de datos"));

        // Enviamos al servicio para que procese la matemática de Haversine y las reglas de negocio
        hitoService.registrarMarcaje(request, usuario.getId(), puntoReferenciaPickup, horaProgramada);

        return new ResponseEntity<>(new MensajeResponseDTO("Marcaje registrado exitosamente"), HttpStatus.CREATED);
    }
}