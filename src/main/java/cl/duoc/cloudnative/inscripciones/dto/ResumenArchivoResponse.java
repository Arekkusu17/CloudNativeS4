package cl.duoc.cloudnative.inscripciones.dto;

public record ResumenArchivoResponse(
        Long inscripcionId,
        String bucket,
        String key,
        String fileName,
        String localPath
) {
}
