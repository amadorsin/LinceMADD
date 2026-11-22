package com.example.apiLince.service;

import com.example.apiLince.dto.request.BloqueoRequestDTO;
import com.example.apiLince.model.BloqueoCalendario;
import com.example.apiLince.model.UsuarioLocal;
import com.example.apiLince.repository.BloqueoCalendarioRepository;
import com.example.apiLince.repository.UsuarioLocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DisponibilidadService {

    private final BloqueoCalendarioRepository bloqueoCalendarioRepository;
    private final UsuarioLocalRepository usuarioLocalRepository;

    public DisponibilidadService(BloqueoCalendarioRepository bloqueoCalendarioRepository, UsuarioLocalRepository usuarioLocalRepository) {
        this.bloqueoCalendarioRepository = bloqueoCalendarioRepository;
        this.usuarioLocalRepository = usuarioLocalRepository;
    }

    @Transactional
    public BloqueoCalendario bloquearFecha(BloqueoRequestDTO request, String usuarioId) {
        UsuarioLocal usuario = usuarioLocalRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        LocalDate fechaHoy = LocalDate.now();
        LocalDate fechaObjetivo = request.getFechaBloqueada();

        // 1. Validación de la regla de los 7 días
        long diasAnticipacion = ChronoUnit.DAYS.between(fechaHoy, fechaObjetivo);
        if (diasAnticipacion < 7) {
            throw new IllegalArgumentException("Solo puedes bloquear días con al menos 7 días de anticipación.");
        }

        // 2. Validación de duplicados
        if (bloqueoCalendarioRepository.existsByUsuarioIdAndFechaBloqueada(usuarioId, fechaObjetivo)) {
            throw new IllegalArgumentException("La fecha " + fechaObjetivo + " ya se encuentra bloqueada.");
        }
        BloqueoCalendario bloqueo = new BloqueoCalendario();
        bloqueo.setUsuario(usuario);
        bloqueo.setFechaBloqueada(fechaObjetivo);
        bloqueo.setFechaSolicitud(fechaHoy);
        bloqueo.setMotivo(request.getMotivo());

        return bloqueoCalendarioRepository.save(bloqueo);
    }

    public List<BloqueoCalendario> obtenerBloqueosUsuario(String usuarioId) {
        return bloqueoCalendarioRepository.findByUsuarioId(usuarioId);
    }
    @Transactional
    public void desbloquearFecha(LocalDate fechaBloqueada, String usuarioId) {
        BloqueoCalendario bloqueo = bloqueoCalendarioRepository
                .findByUsuarioIdAndFechaBloqueada(usuarioId, fechaBloqueada)
                .orElseThrow(() -> new IllegalArgumentException("No existe un bloqueo registrado para la fecha " + fechaBloqueada));

        bloqueoCalendarioRepository.delete(bloqueo);
    }
}