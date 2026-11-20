package com.example.apiLince.repository;

import com.example.apiLince.model.ConfirmacionServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfirmacionServicioRepository extends JpaRepository<ConfirmacionServicio, Long> {
    Optional<ConfirmacionServicio> findByServicioIdAndUsuarioId(String servicioId, String usuarioId);
    List<ConfirmacionServicio> findByServicioId(String servicioId);
    List<ConfirmacionServicio> findByUsuarioId(String usuarioId);
}