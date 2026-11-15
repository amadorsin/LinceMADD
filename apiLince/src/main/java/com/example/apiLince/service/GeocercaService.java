package cl.duoc.linceops.backend.service;

import cl.duoc.linceops.backend.model.Geocerca;
import cl.duoc.linceops.backend.repository.GeocercaRepository;
import org.springframework.stereotype.Service;

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

        // Retornamos en metros
        return RADIO_TIERRA_KM * c * 1000;
    }

    /**
     * Valida si una coordenada está dentro del radio permitido del punto de referencia.
     */
    public boolean validarDentroDeGeocerca(String puntoReferencia, double latUsuario, double lonUsuario) {
        // Buscamos el hotel/aeropuerto en la base de datos
        Geocerca geocerca = geocercaRepository.findByPuntoReferencia(puntoReferencia)
                .orElseThrow(() -> new IllegalArgumentException("Punto de referencia no encontrado en el sistema"));

        double distancia = calcularDistanciaMetros(
                geocerca.getLatitudCentro(), geocerca.getLongitudCentro(),
                latUsuario, lonUsuario
        );

        // Retorna true si está dentro de los 150m (o el radio configurado)[cite: 47]
        return distancia <= geocerca.getRadioMetros();
    }
}