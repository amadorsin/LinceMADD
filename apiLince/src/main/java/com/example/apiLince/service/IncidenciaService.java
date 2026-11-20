package com.example.apiLince.service;

import com.example.apiLince.dto.request.IncidenciaRequestDTO;
import com.example.apiLince.model.Incidencia;
import com.example.apiLince.model.UsuarioLocal;
import com.example.apiLince.repository.IncidenciaRepository;
import com.example.apiLince.repository.UsuarioLocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioLocalRepository usuarioLocalRepository;

    public IncidenciaService(IncidenciaRepository incidenciaRepository, UsuarioLocalRepository usuarioLocalRepository) {
        this.incidenciaRepository = incidenciaRepository;
        this.usuarioLocalRepository = usuarioLocalRepository;
    }

    @Transactional
    public Incidencia registrarIncidenciaManual(IncidenciaRequestDTO request, String usuarioId) {
        UsuarioLocal usuario = usuarioLocalRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        Incidencia incidencia = new Incidencia();
        incidencia.setServicioId(request.getServicioId());
        incidencia.setUsuario(usuario);
        incidencia.setMotivo(request.getMotivo());
        incidencia.setDetalle(request.getDetalle());
        incidencia.setFechaHora(LocalDateTime.now());

        return incidenciaRepository.save(incidencia);
    }
    public List<Incidencia> obtenerTodasLasIncidencias() {
        return incidenciaRepository.findAll();
    }
    public List<Incidencia> obtenerIncidenciasPorServicio(String servicioId) {
        return incidenciaRepository.findByServicioId(servicioId);
    }
}