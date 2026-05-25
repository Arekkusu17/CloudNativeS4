package cl.duoc.cloudnative.inscripciones.repository;

import cl.duoc.cloudnative.inscripciones.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
