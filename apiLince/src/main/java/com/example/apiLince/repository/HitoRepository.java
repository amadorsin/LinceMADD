package cl.duoc.linceops.backend.repository;

import cl.duoc.linceops.backend.model.Hito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HitoRepository extends JpaRepository<Hito, Long> {
    List<Hito> findByServicioId(String servicioId);

    // Validamos la idempotencia (evita que se registre dos veces el mismo check-in si la app sincroniza doble)
    boolean existsByServicioIdAndUsuarioIdAndTipo(String servicioId, String usuarioId, String tipo);
}