package cl.duoc.cloudnative.inscripciones.service;

import cl.duoc.cloudnative.inscripciones.dto.CursoRequest;
import cl.duoc.cloudnative.inscripciones.dto.CursoResponse;
import cl.duoc.cloudnative.inscripciones.model.Curso;
import cl.duoc.cloudnative.inscripciones.repository.CursoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    public List<CursoResponse> listarCursos() {
        return cursoRepository.findAll()
                .stream()
                .map(CursoResponse::from)
                .toList();
    }

    public CursoResponse crearCurso(CursoRequest request) {
        validarCurso(request);
        Curso curso = new Curso(
                request.nombre().trim(),
                request.instructor().trim(),
                request.duracionHoras(),
                request.costo()
        );
        return CursoResponse.from(cursoRepository.save(curso));
    }

    private void validarCurso(CursoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El curso es obligatorio.");
        }
        if (esTextoVacio(request.nombre())) {
            throw new IllegalArgumentException("El nombre del curso es obligatorio.");
        }
        if (esTextoVacio(request.instructor())) {
            throw new IllegalArgumentException("El instructor es obligatorio.");
        }
        if (request.duracionHoras() == null || request.duracionHoras() <= 0) {
            throw new IllegalArgumentException("La duracion debe ser mayor a cero horas.");
        }
        if (request.costo() == null || request.costo().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo debe ser mayor o igual a cero.");
        }
    }

    private boolean esTextoVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
