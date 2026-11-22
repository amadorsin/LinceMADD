package com.example.apiLince.service;

import com.example.apiLince.dto.request.MarcajeRequestDTO;
import com.example.apiLince.exception.GeocercaValidationException;
import com.example.apiLince.exception.HorarioInvalidoException;
import com.example.apiLince.model.Hito;
import com.example.apiLince.model.Incidencia;
import com.example.apiLince.model.UsuarioLocal;
import com.example.apiLince.repository.HitoRepository;
import com.example.apiLince.repository.IncidenciaRepository;
import com.example.apiLince.repository.UsuarioLocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
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

    @Transactional(noRollbackFor = {
            GeocercaValidationException.class,
            HorarioInvalidoException.class
    })
    public Hito registrarMarcaje(MarcajeRequestDTO request, String usuarioId, String pickup, String oficina, LocalDateTime horaPickup) {

        UsuarioLocal usuario = usuarioLocalRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no autorizado"));

        // 1. Idempotencia: Evitar doble marcaje si WorkManager reintenta el envío
        if (hitoRepository.existsByServicioIdAndUsuarioIdAndTipo(request.getServicioId(), usuarioId, request.getTipo())) {
            throw new IllegalArgumentException("El " + request.getTipo() + " ya fue registrado para este servicio.");
        }

        // 2. Validaciones solo para CHECK-IN
        if ("CHECK_IN".equalsIgnoreCase(request.getTipo())) {

            // A. Validación de Geocerca (150 metros)
            boolean enRango = geocercaService.validarDentroDeGeocerca(pickup, oficina, request.getLatitud(), request.getLongitud());
            if (!enRango) {
                registrarIncidencia(request.getServicioId(), usuario, "FUERA_DE_RADIO", "Usuario intentó marcar a más de 150m del punto: " + pickup);
                throw new GeocercaValidationException("Estás fuera de la geocerca permitida. Acércate al punto de encuentro.");
            }

            // B. Validación de Tiempo (15 minutos antes de la hora programada)
            if (horaPickup != null) {
                long minutosDiferencia = ChronoUnit.MINUTES.between(request.getTimestamp(), horaPickup);
                if (minutosDiferencia < 15) {
                    registrarIncidencia(request.getServicioId(), usuario, "ATRASO", "Marcaje realizado con menos de 15 minutos de anticipación.");
                    throw new HorarioInvalidoException("Llegada tardía. Debes presentarte al menos 15 minutos antes.");
                }
            }
        }
        if ("CHECK_OUT".equalsIgnoreCase(request.getTipo())) {
            boolean tieneCheckIn = hitoRepository.existsByServicioIdAndUsuarioIdAndTipo(
                    request.getServicioId(), usuarioId, "CHECK_IN");
            if (!tieneCheckIn) {
                throw new IllegalArgumentException("No puedes registrar Check-Out sin un Check-In válido previo.");
            }
        }
        // 3. Guardar Hito
        Hito hito = new Hito();
        hito.setServicioId(request.getServicioId());
        hito.setUsuario(usuario);
        hito.setTipo(request.getTipo().toUpperCase());
        hito.setTimestampRegistro(request.getTimestamp());
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
    public List<Hito> obtenerTodosLosHitos() {
        return hitoRepository.findAll();
    }
}