package com.example.apiLince.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class BloqueoRequestDTO {

    @NotNull(message = "La fecha a bloquear es obligatoria")
    private LocalDate fechaBloqueada;

    @NotBlank(message = "El motivo del bloqueo es obligatorio")
    private String motivo;

    // Getters y Setters
    public LocalDate getFechaBloqueada() { return fechaBloqueada; }
    public void setFechaBloqueada(LocalDate fechaBloqueada) { this.fechaBloqueada = fechaBloqueada; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}