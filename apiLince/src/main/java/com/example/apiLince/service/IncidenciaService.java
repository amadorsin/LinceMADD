package cl.duoc.linceops.backend.service;

import cl.duoc.linceops.backend.dto.request.IncidenciaRequestDTO;
import cl.duoc.linceops.backend.model.Incidencia;
import cl.duoc.linceops.backend.model.UsuarioLocal;
import cl.duoc.linceops.backend.repository.IncidenciaRepository;
import cl.duoc.linceops.backend.repository.UsuarioLocalRepository;
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

    public List<Incidencia> obtenerIncidenciasPorServicio(String servicioId) {
        return incidenciaRepository.findByServicioId(servicioId);
    }
}