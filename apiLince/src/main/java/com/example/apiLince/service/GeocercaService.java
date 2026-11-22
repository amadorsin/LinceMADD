package com.example.apiLince.service;

import com.example.apiLince.model.Geocerca;
import com.example.apiLince.repository.GeocercaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GeocercaService {

    private final GeocercaRepository geocercaRepository;
    private static final int RADIO_TIERRA_KM = 6371;

    public GeocercaService(GeocercaRepository geocercaRepository) {
        this.geocercaRepository = geocercaRepository;
    }

    /**
     * Calcula la distancia en metros entre dos coordenadas usando Haversine.
     */
    public double calcularDistanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c * 1000;
    }

    /**
     * Valida si una coordenada está dentro del radio permitido del pickup (o de la oficina como respaldo).
     */
    public boolean validarDentroDeGeocerca(String pickup, String oficina, double latUsuario, double lonUsuario) {
        Optional<Geocerca> geocercaOpt = Optional.empty();

        if (pickup != null && !pickup.isBlank()) {
            geocercaOpt = geocercaRepository.findByPuntoReferencia(pickup.trim());
        }

        // Respaldo por código de oficina de Southbound (SCL, PUX, PUQ)
        if (geocercaOpt.isEmpty() && oficina != null && !oficina.isBlank()) {
            geocercaOpt = geocercaRepository.findByPuntoReferencia(oficina.trim().toUpperCase());
        }

        Geocerca geocerca = geocercaOpt.orElseThrow(
                () -> new IllegalArgumentException("Punto de referencia '" + pickup + "' no encontrado en el sistema")
        );

        double distancia = calcularDistanciaMetros(
                geocerca.getLatitudCentro(), geocerca.getLongitudCentro(),
                latUsuario, lonUsuario
        );

        return distancia <= geocerca.getRadioMetros();
    }
}