package com.example.apiLince.controller;

import com.example.apiLince.model.Geocerca;
import com.example.apiLince.repository.GeocercaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geocercas")
@Tag(name = "Geocercas", description = "Consulta y configuración de puntos de referencia y radios")
public class GeocercaController {

    private final GeocercaRepository geocercaRepository;

    public GeocercaController(GeocercaRepository geocercaRepository) {
        this.geocercaRepository = geocercaRepository;
    }

    @GetMapping
    @Operation(summary = "Listar geocercas", description = "Retorna todos los puntos de referencia con sus coordenadas y radio en metros")
    public ResponseEntity<List<Geocerca>> listarTodas() {
        return ResponseEntity.ok(geocercaRepository.findAll());
    }

    @PutMapping("/{id}/radio")
    @Operation(summary = "Actualizar radio", description = "Modifica el radio permitido en metros para una geocerca")
    public ResponseEntity<Geocerca> actualizarRadio(@PathVariable Long id, @RequestParam Integer radioMetros) {
        Geocerca geocerca = geocercaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Geocerca no encontrada"));
        geocerca.setRadioMetros(radioMetros);
        return ResponseEntity.ok(geocercaRepository.save(geocerca));
    }
}