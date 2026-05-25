package cl.duoc.cloudnative.inscripciones.service;

import cl.duoc.cloudnative.inscripciones.dto.InscripcionRequest;
import cl.duoc.cloudnative.inscripciones.dto.InscripcionResponse;
import cl.duoc.cloudnative.inscripciones.model.Curso;
import cl.duoc.cloudnative.inscripciones.model.Inscripcion;
import cl.duoc.cloudnative.inscripciones.repository.CursoRepository;
import cl.duoc.cloudnative.inscripciones.repository.InscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InscripcionService {

    private final CursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;

    public InscripcionService(CursoRepository cursoRepository, InscripcionRepository inscripcionRepository) {
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Transactional
    public InscripcionResponse crearInscripcion(InscripcionRequest request) {
        validarInscripcion(request);

        List<Long> idsUnicos = request.cursoIds()
                .stream()
                .distinct()
                .toList();
        List<Curso> cursos = cursoRepository.findAllById(idsUnicos);

        if (cursos.size() != idsUnicos.size()) {
            throw new IllegalArgumentException("Uno o mas cursos seleccionados no existen.");
        }

        BigDecimal total = cursos.stream()
                .map(Curso::getCosto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Inscripcion inscripcion = new Inscripcion(
                request.estudianteNombre().trim(),
                request.estudianteEmail().trim(),
                cursos,
                total
        );

        return InscripcionResponse.from(inscripcionRepository.save(inscripcion));
    }

    public List<InscripcionResponse> listarInscripciones() {
        return inscripcionRepository.findAll()
                .stream()
                .map(InscripcionResponse::from)
                .toList();
    }

    private void validarInscripcion(InscripcionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La inscripcion es obligatoria.");
        }
        if (esTextoVacio(request.estudianteNombre())) {
            throw new IllegalArgumentException("El nombre del estudiante es obligatorio.");
        }
        if (esTextoVacio(request.estudianteEmail())) {
            throw new IllegalArgumentException("El email del estudiante es obligatorio.");
        }
        if (request.cursoIds() == null || request.cursoIds().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos un curso.");
        }
        if (request.cursoIds().stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Los ids de cursos deben ser validos.");
        }
    }

    private boolean esTextoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
