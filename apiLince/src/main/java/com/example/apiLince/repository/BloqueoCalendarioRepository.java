package com.example.apiLince.repository;

import com.example.apiLince.model.BloqueoCalendario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloqueoCalendarioRepository extends JpaRepository<BloqueoCalendario, Long> {
    List<BloqueoCalendario> findByUsuarioId(String usuarioId);

    // Para evitar que el usuario bloquee el mismo día dos veces
    boolean existsByUsuarioIdAndFechaBloqueada(String usuarioId, LocalDate fechaBloqueada);
}