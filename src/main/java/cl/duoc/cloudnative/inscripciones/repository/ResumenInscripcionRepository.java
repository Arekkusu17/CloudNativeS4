package cl.duoc.cloudnative.inscripciones.repository;

import cl.duoc.cloudnative.inscripciones.model.ResumenInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumenInscripcionRepository extends JpaRepository<ResumenInscripcion, Long> {

    Optional<ResumenInscripcion> findByInscripcionId(Long inscripcionId);
}
