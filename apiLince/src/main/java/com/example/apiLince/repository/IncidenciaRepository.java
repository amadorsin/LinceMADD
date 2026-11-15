package cl.duoc.linceops.backend.repository;

import cl.duoc.linceops.backend.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
    List<Incidencia> findByServicioId(String servicioId);
}