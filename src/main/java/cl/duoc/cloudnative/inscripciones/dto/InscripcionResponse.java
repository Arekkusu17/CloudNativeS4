package cl.duoc.cloudnative.inscripciones.dto;

import cl.duoc.cloudnative.inscripciones.model.Inscripcion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InscripcionResponse(
        Long id,
        String estudianteNombre,
        String estudianteEmail,
        List<CursoResponse> cursos,
        BigDecimal total,
        LocalDateTime fechaInscripcion
) {
    public static InscripcionResponse from(Inscripcion inscripcion) {
        return new InscripcionResponse(
                inscripcion.getId(),
                inscripcion.getEstudianteNombre(),
                inscripcion.getEstudianteEmail(),
                inscripcion.getCursos().stream().map(CursoResponse::from).toList(),
                inscripcion.getTotal(),
                inscripcion.getFechaInscripcion()
        );
    }
}
