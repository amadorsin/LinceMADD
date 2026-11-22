package com.example.apiLince.service;

import com.example.apiLince.dto.request.ConfirmacionRequestDTO;
import com.example.apiLince.model.ConfirmacionServicio;
import com.example.apiLince.model.UsuarioLocal;
import com.example.apiLince.repository.ConfirmacionServicioRepository;
import com.example.apiLince.repository.UsuarioLocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConfirmacionServicioService {

    private final ConfirmacionServicioRepository confirmacionRepository;
    private final UsuarioLocalRepository usuarioLocalRepository;

    public ConfirmacionServicioService(ConfirmacionServicioRepository confirmacionRepository,
                                       UsuarioLocalRepository usuarioLocalRepository) {
        this.confirmacionRepository = confirmacionRepository;
        this.usuarioLocalRepository = usuarioLocalRepository;
    }

    @Transactional
    public ConfirmacionServicio registrarConfirmacion(ConfirmacionRequestDTO request, String usuarioId) {
        UsuarioLocal usuario = usuarioLocalRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        String estadoNormalizado = request.getEstado().trim().toUpperCase();
        if (!estadoNormalizado.equals("CONFIRMADO") && !estadoNormalizado.equals("RECHAZADO")) {
            throw new IllegalArgumentException("Estado inválido. Usa CONFIRMADO o RECHAZADO.");
        }
        if (estadoNormalizado.equals("RECHAZADO") && (request.getMotivo() == null || request.getMotivo().isBlank())) {
            throw new IllegalArgumentException("Debes indicar un motivo al rechazar un servicio.");
        }

        // Idempotencia: si ya existe un registro para este usuario y servicio, se actualiza
        ConfirmacionServicio confirmacion = confirmacionRepository
                .findByServicioIdAndUsuarioId(request.getServicioId(), usuarioId)
                .orElseGet(ConfirmacionServicio::new);

        confirmacion.setServicioId(request.getServicioId());
        confirmacion.setUsuario(usuario);
        confirmacion.setEstado(estadoNormalizado);
        confirmacion.setMotivo(estadoNormalizado.equals("RECHAZADO") ? request.getMotivo().trim() : null);
        confirmacion.setFechaHora(LocalDateTime.now());

        return confirmacionRepository.save(confirmacion);
    }

    public List<ConfirmacionServicio> obtenerPorServicio(String servicioId) {
        return confirmacionRepository.findByServicioId(servicioId);
    }

    public List<ConfirmacionServicio> obtenerPorUsuario(String usuarioId) {
        return confirmacionRepository.findByUsuarioId(usuarioId);
    }
}