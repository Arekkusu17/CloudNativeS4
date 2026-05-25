package cl.duoc.cloudnative.inscripciones.dto;

import java.math.BigDecimal;

public record CursoRequest(
        String nombre,
        String instructor,
        Integer duracionHoras,
        BigDecimal costo
) {
}
