package cl.duoc.cloudnative.inscripciones.repository;

import cl.duoc.cloudnative.inscripciones.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
}
