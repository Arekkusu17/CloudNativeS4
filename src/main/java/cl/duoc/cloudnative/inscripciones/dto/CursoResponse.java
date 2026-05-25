package cl.duoc.cloudnative.inscripciones.dto;

import cl.duoc.cloudnative.inscripciones.model.Curso;

import java.math.BigDecimal;

public record CursoResponse(
        Long id,
        String nombre,
        String instructor,
        Integer duracionHoras,
        BigDecimal costo
) {
    public static CursoResponse from(Curso curso) {
        return new CursoResponse(
                curso.getId(),
                curso.getNombre(),
                curso.getInstructor(),
                curso.getDuracionHoras(),
                curso.getCosto()
        );
    }
}
