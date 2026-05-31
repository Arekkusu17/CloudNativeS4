package cl.duoc.cloudnative.inscripciones.service;

import cl.duoc.cloudnative.inscripciones.dto.ResumenArchivoResponse;
import cl.duoc.cloudnative.inscripciones.model.Curso;
import cl.duoc.cloudnative.inscripciones.model.Inscripcion;
import cl.duoc.cloudnative.inscripciones.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
public class ResumenInscripcionService {

    private static final String CONTENT_TYPE = "text/plain; charset=UTF-8";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final InscripcionRepository inscripcionRepository;
    private final S3Client s3Client;
    private final String resumenesBucket;
    private final Path resumenesLocalDir;

    public ResumenInscripcionService(
            InscripcionRepository inscripcionRepository,
            S3Client s3Client,
            @Value("${app.aws.s3.resumenes-bucket}") String resumenesBucket,
            @Value("${app.resumenes.local-dir}") String resumenesLocalDir) {
        this.inscripcionRepository = inscripcionRepository;
        this.s3Client = s3Client;
        this.resumenesBucket = resumenesBucket;
        this.resumenesLocalDir = Paths.get(resumenesLocalDir);
    }

    public byte[] generarResumen(Long inscripcionId) {
        Inscripcion inscripcion = obtenerInscripcion(inscripcionId);
        return construirResumen(inscripcion).getBytes(StandardCharsets.UTF_8);
    }

    public String guardarResumenLocal(Inscripcion inscripcion) {
        Path ruta = rutaResumenLocal(inscripcion.getId());
        try {
            Files.createDirectories(ruta.getParent());
            Files.writeString(ruta, construirResumen(inscripcion), StandardCharsets.UTF_8);
            return ruta.normalize().toString();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible crear el archivo fisico del resumen.");
        }
    }

    public byte[] descargarResumenLocal(Long inscripcionId) {
        Inscripcion inscripcion = obtenerInscripcion(inscripcionId);
        Path ruta = rutaResumenLocal(inscripcionId);

        try {
            if (Files.notExists(ruta)) {
                guardarResumenLocal(inscripcion);
            }
            return Files.readAllBytes(ruta);
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible leer el archivo fisico del resumen.");
        }
    }

    public String nombreArchivo(Long inscripcionId) {
        return "resumen-inscripcion.txt";
    }

    public ResumenArchivoResponse subirResumenGenerado(Long inscripcionId) {
        byte[] contenido = generarResumen(inscripcionId);
        String key = keyResumen(inscripcionId);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(obtenerBucket())
                .key(key)
                .contentType(CONTENT_TYPE)
                .contentLength((long) contenido.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(contenido));
        return respuesta(inscripcionId, key);
    }

    public ResumenArchivoResponse reemplazarResumen(Long inscripcionId, MultipartFile file) {
        obtenerInscripcion(inscripcionId);
        validarArchivo(file);
        String key = keyResumen(inscripcionId);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(obtenerBucket())
                    .key(key)
                    .contentType(file.getContentType() != null ? file.getContentType() : CONTENT_TYPE)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return respuesta(inscripcionId, key);
        } catch (IOException exception) {
            throw new IllegalArgumentException("No fue posible leer el archivo enviado.");
        }
    }

    public byte[] descargarResumenDesdeS3(Long inscripcionId) {
        obtenerInscripcion(inscripcionId);
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(obtenerBucket())
                .key(keyResumen(inscripcionId))
                .build();

        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
        return response.asByteArray();
    }

    public void borrarResumen(Long inscripcionId) {
        obtenerInscripcion(inscripcionId);
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(obtenerBucket())
                .key(keyResumen(inscripcionId))
                .build();

        s3Client.deleteObject(request);
    }

    private Inscripcion obtenerInscripcion(Long inscripcionId) {
        if (inscripcionId == null || inscripcionId <= 0) {
            throw new IllegalArgumentException("El numero de inscripcion debe ser valido.");
        }
        return inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La inscripcion indicada no existe."));
    }

    private String construirResumen(Inscripcion inscripcion) {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Resumen de inscripcion Nro. ").append(inscripcion.getId()).append(System.lineSeparator());
        resumen.append("Fecha de inscripcion: ")
                .append(inscripcion.getFechaInscripcion().format(DATE_FORMAT))
                .append(System.lineSeparator());
        resumen.append("Estudiante: ").append(inscripcion.getEstudianteNombre()).append(System.lineSeparator());
        resumen.append("Email: ").append(inscripcion.getEstudianteEmail()).append(System.lineSeparator());
        resumen.append(System.lineSeparator());
        resumen.append("Cursos inscritos:").append(System.lineSeparator());

        for (Curso curso : inscripcion.getCursos()) {
            resumen.append("- ")
                    .append(curso.getNombre())
                    .append(" | Instructor: ")
                    .append(curso.getInstructor())
                    .append(" | Duracion: ")
                    .append(curso.getDuracionHoras())
                    .append(" horas | Costo: $")
                    .append(formatearMonto(curso.getCosto()))
                    .append(System.lineSeparator());
        }

        resumen.append(System.lineSeparator());
        resumen.append("Total a pagar: $").append(formatearMonto(inscripcion.getTotal())).append(System.lineSeparator());
        return resumen.toString();
    }

    private String formatearMonto(BigDecimal monto) {
        return monto.stripTrailingZeros().toPlainString();
    }

    private String keyResumen(Long inscripcionId) {
        return "inscripciones/id=" + inscripcionId + "/" + nombreArchivo(inscripcionId);
    }

    private Path rutaResumenLocal(Long inscripcionId) {
        return resumenesLocalDir
                .resolve("inscripciones")
                .resolve("id=" + inscripcionId)
                .resolve(nombreArchivo(inscripcionId));
    }

    private ResumenArchivoResponse respuesta(Long inscripcionId, String key) {
        return new ResumenArchivoResponse(inscripcionId, obtenerBucket(), key, nombreArchivo(inscripcionId));
    }

    private String obtenerBucket() {
        if (resumenesBucket == null || resumenesBucket.trim().isEmpty()) {
            throw new IllegalStateException("Debe configurar AWS_S3_RESUMENES_BUCKET para usar S3.");
        }
        return resumenesBucket.trim();
    }

    private void validarArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar un archivo de resumen.");
        }
    }
}
