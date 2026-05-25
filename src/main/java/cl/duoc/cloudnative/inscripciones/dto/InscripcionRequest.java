package cl.duoc.cloudnative.inscripciones.dto;

import java.util.List;

public record InscripcionRequest(
        String estudianteNombre,
        String estudianteEmail,
        List<Long> cursoIds
) {
}
