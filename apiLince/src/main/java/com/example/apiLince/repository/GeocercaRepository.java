package com.example.apiLince.repository;

import com.example.apiLince.model.Geocerca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GeocercaRepository extends JpaRepository<Geocerca, Long> {
    // Para buscar el hotel o aeropuerto y obtener su lat/lon central y su radio (150m)
    Optional<Geocerca> findByPuntoReferencia(String puntoReferencia);
}