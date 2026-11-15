package cl.duoc.linceops.backend.service;

import cl.duoc.linceops.backend.dto.request.MarcajeRequestDTO;
import cl.duoc.linceops.backend.model.Hito;
import cl.duoc.linceops.backend.model.Incidencia;
import cl.duoc.linceops.backend.model.UsuarioLocal;
import cl.duoc.linceops.backend.repository.HitoRepository;
import cl.duoc.linceops.backend.repository.IncidenciaRepository;
import cl.duoc.linceops.backend.repository.UsuarioLocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class HitoService {

    private final HitoRepository hitoRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioLocalRepository usuarioLocalRepository;
    private final GeocercaService geocercaService;

    public HitoService(HitoRepository hitoRepository,
                       IncidenciaRepository incidenciaRepository,
                       UsuarioLocalRepository usuarioLocalRepository,
                       GeocercaService geocercaService) {
        this.hitoRepository = hitoRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.usuarioLocalRepository = usuarioLocalRepository;
        this.geocercaService = geocercaService;
    }

    @Transactional
    public Hito registrarMarcaje(MarcajeRequestDTO request, String usuarioId, String puntoReferenciaPickup, LocalDateTime horaProgramada) {

        UsuarioLocal usuario = usuarioLocalRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no autorizado"));

        // 1. Idempotencia: Evitar doble marcaje si el WorkManager del móvil reintenta el envío[cite: 48]
        if (hitoRepository.existsByServicioIdAndUsuarioIdAndTipo(request.getServicioId(), usuarioId, request.getTipo())) {
            throw new IllegalArgumentException("El " + request.getTipo() + " ya fue registrado para este servicio.");
        }

        // 2. Validaciones solo para CHECK-IN
        if ("CHECK_IN".equalsIgnoreCase(request.getTipo())) {

            // A. Validación de Geocerca (150 metros)[cite: 47, 48]
            boolean enRango = geocercaService.validarDentroDeGeocerca(puntoReferenciaPickup, request.getLatitud(), request.getLongitud());
            if (!enRango) {
                registrarIncidencia(request.getServicioId(), usuario, "FUERA_DE_RADIO", "Usuario intentó marcar a más de 150m del punto.");
                throw new IllegalStateException("Estás fuera de la geocerca permitida. Acércate al punto de encuentro.");
            }

            // B. Validación de Tiempo (15 minutos antes de la hora programada)[cite: 47, 48]
            // Comparamos la hora exacta del servidor (o el timestamp validado) contra la hora programada del tour
            long minutosDiferencia = ChronoUnit.MINUTES.between(request.getTimestamp(), horaProgramada);

            // Si faltan menos de 15 minutos (o ya se pasó la hora), está atrasado para su estándar operativo
            if (minutosDiferencia < 15) {
                registrarIncidencia(request.getServicioId(), usuario, "ATRASO", "Marcaje realizado con menos de 15 minutos de anticipación.");
                throw new IllegalStateException("Llegada tardía. Debes presentarte al menos 15 minutos antes.");
            }
        }

        // 3. Si pasa las validaciones (o es Check-out), guardamos el Hito
        Hito hito = new Hito();
        hito.setServicioId(request.getServicioId());
        hito.setUsuario(usuario);
        hito.setTipo(request.getTipo().toUpperCase());
        hito.setTimestampRegistro(request.getTimestamp()); // Se prioriza la hora del servidor en un caso real, pero usamos la del dto para offline
        hito.setLatitudMarcaje(request.getLatitud());
        hito.setLongitudMarcaje(request.getLongitud());

        return hitoRepository.save(hito);
    }

    private void registrarIncidencia(String servicioId, UsuarioLocal usuario, String motivo, String detalle) {
        Incidencia incidencia = new Incidencia();
        incidencia.setServicioId(servicioId);
        incidencia.setUsuario(usuario);
        incidencia.setMotivo(motivo);
        incidencia.setDetalle(detalle);
        incidencia.setFechaHora(LocalDateTime.now());
        incidenciaRepository.save(incidencia);
    }
}