package com.example.apiLince.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ConfirmacionRequestDTO {

    @NotBlank(message = "El servicioId es obligatorio")
    private String servicioId;

    @NotBlank(message = "El estado es obligatorio (CONFIRMADO o RECHAZADO)")
    private String estado;

    private String motivo;

    public ConfirmacionRequestDTO() {}

    public String getServicioId() { return servicioId; }
    public void setServicioId(String servicioId) { this.servicioId = servicioId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}