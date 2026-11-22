package com.example.apiLince.controller;

import com.example.apiLince.dto.request.IncidenciaRequestDTO;
import com.example.apiLince.model.Incidencia;
import com.example.apiLince.model.UsuarioLocal;
import com.example.apiLince.repository.UsuarioLocalRepository;
import com.example.apiLince.service.IncidenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
@Tag(name = "Incidencias", description = "Endpoints para registro y consulta de atrasos o desvíos de geocerca")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;
    private final UsuarioLocalRepository usuarioLocalRepository;

    public IncidenciaController(IncidenciaService incidenciaService, UsuarioLocalRepository usuarioLocalRepository) {
        this.incidenciaService = incidenciaService;
        this.usuarioLocalRepository = usuarioLocalRepository;
    }
    @GetMapping
    @Operation(summary = "Listar todas las incidencias", description = "Retorna todas las incidencias registradas para coordinación")
    public ResponseEntity<List<Incidencia>> listarTodas() {
        return ResponseEntity.ok(incidenciaService.obtenerTodasLasIncidencias());
    }
    @PostMapping("/registrar")
    @Operation(summary = "Registrar incidencia manual", description = "Permite a un usuario registrar un problema asociado a un servicio")
    public ResponseEntity<Incidencia> registrarIncidencia(@Valid @RequestBody IncidenciaRequestDTO request, Principal principal) {
        UsuarioLocal usuario = usuarioLocalRepository.findByCorreo(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Incidencia incidencia = incidenciaService.registrarIncidenciaManual(request, usuario.getId());
        return new ResponseEntity<>(incidencia, HttpStatus.CREATED);
    }

    @GetMapping("/servicio/{servicioId}")
    @Operation(summary = "Ver incidencias por servicio", description = "Retorna la lista de incidencias para un servicio específico")
    public ResponseEntity<List<Incidencia>> obtenerIncidencias(@PathVariable String servicioId) {
        List<Incidencia> lista = incidenciaService.obtenerIncidenciasPorServicio(servicioId);
        // Retornamos 200 OK siempre. Si está vacía, devuelve un [] vacío, tal como arreglamos en FixNow.
        return ResponseEntity.ok(lista);
    }
}