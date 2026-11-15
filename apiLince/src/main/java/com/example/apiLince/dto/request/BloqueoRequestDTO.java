package cl.duoc.linceops.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class BloqueoRequestDTO {

    @NotNull(message = "La fecha a bloquear es obligatoria")
    private LocalDate fechaBloqueada;

    // Getters y Setters
    public LocalDate getFechaBloqueada() { return fechaBloqueada; }
    public void setFechaBloqueada(LocalDate fechaBloqueada) { this.fechaBloqueada = fechaBloqueada; }
}