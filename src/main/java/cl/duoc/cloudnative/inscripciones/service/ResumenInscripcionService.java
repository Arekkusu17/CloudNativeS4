package cl.duoc.cloudnative.inscripciones.service;

import cl.duoc.cloudnative.inscripciones.dto.ResumenArchivoResponse;
import cl.duoc.cloudnative.inscripciones.model.Curso;
import cl.duoc.cloudnative.inscripciones.model.Inscripcion;
import cl.duoc.cloudnative.inscripciones.model.ResumenInscripcion;
import cl.duoc.cloudnative.inscripciones.repository.InscripcionRepository;
import cl.duoc.cloudnative.inscripciones.repository.ResumenInscripcionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final ResumenInscripcionRepository resumenInscripcionRepository;
    private final S3Client s3Client;
    private final String resumenesBucket;
    private final Path resumenesLocalDir;

    public ResumenInscripcionService(
            InscripcionRepository inscripcionRepository,
            ResumenInscripcionRepository resumenInscripcionRepository,
            S3Client s3Client,
            @Value("${app.aws.s3.resumenes-bucket}") String resumenesBucket,
            @Value("${app.resumenes.local-dir}") String resumenesLocalDir) {
        this.inscripcionRepository = inscripcionRepository;
        this.resumenInscripcionRepository = resumenInscripcionRepository;
        this.s3Client = s3Client;
        this.resumenesBucket = resumenesBucket;
        this.resumenesLocalDir = Paths.get(resumenesLocalDir);
    }

    public byte[] generarResumen(Long inscripcionId) {
        Inscripcion inscripcion = obtenerInscripcion(inscripcionId);
        return construirResumen(inscripcion).getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public String guardarResumenLocal(Inscripcion inscripcion) {
        Path ruta = rutaResumenLocal(inscripcion.getId());
        try {
            Files.createDirectories(ruta.getParent());
            Files.writeString(ruta, construirResumen(inscripcion), StandardCharsets.UTF_8);
            String rutaNormalizada = ruta.normalize().toString();
            ResumenInscripcion resumen = obtenerOCrearResumen(inscripcion);
            resumen.actualizarArchivoLocal(nombreArchivo(inscripcion.getId()), rutaNormalizada);
            resumenInscripcionRepository.save(resumen);
            return rutaNormalizada;
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

    @Transactional
    public ResumenArchivoResponse subirResumenGenerado(Long inscripcionId) {
        Inscripcion inscripcion = obtenerInscripcion(inscripcionId);
        byte[] contenido = construirResumen(inscripcion).getBytes(StandardCharsets.UTF_8);
        String key = keyResumen(inscripcionId);
        String bucket = obtenerBucket();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(CONTENT_TYPE)
                .contentLength((long) contenido.length)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(contenido));
        String rutaLocal = guardarResumenLocal(inscripcion);
        ResumenInscripcion resumen = obtenerOCrearResumen(inscripcion);
        resumen.actualizarArchivoLocal(nombreArchivo(inscripcionId), rutaLocal);
        resumen.actualizarS3(bucket, key);
        resumenInscripcionRepository.save(resumen);
        return respuesta(inscripcionId, bucket, key, rutaLocal);
    }

    @Transactional
    public ResumenArchivoResponse reemplazarResumen(Long inscripcionId, MultipartFile file) {
        Inscripcion inscripcion = obtenerInscripcion(inscripcionId);
        validarArchivo(file);
        String key = keyResumen(inscripcionId);
        String bucket = obtenerBucket();

        try {
            byte[] contenido = file.getBytes();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType() != null ? file.getContentType() : CONTENT_TYPE)
                    .contentLength((long) contenido.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(contenido));
            Path ruta = rutaResumenLocal(inscripcionId);
            Files.createDirectories(ruta.getParent());
            Files.write(ruta, contenido);

            ResumenInscripcion resumen = obtenerOCrearResumen(inscripcion);
            resumen.actualizarArchivoLocal(nombreArchivo(inscripcionId), ruta.normalize().toString());
            resumen.actualizarS3(bucket, key);
            resumenInscripcionRepository.save(resumen);
            return respuesta(inscripcionId, bucket, key, resumen.getRutaLocal());
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

    @Transactional
    public void borrarResumen(Long inscripcionId) {
        Inscripcion inscripcion = obtenerInscripcion(inscripcionId);
        String bucket = obtenerBucket();
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(keyResumen(inscripcionId))
                .build();

        s3Client.deleteObject(request);
        ResumenInscripcion resumen = obtenerOCrearResumen(inscripcion);
        resumen.limpiarS3();
        resumenInscripcionRepository.save(resumen);
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

    private ResumenInscripcion obtenerOCrearResumen(Inscripcion inscripcion) {
        return resumenInscripcionRepository.findByInscripcionId(inscripcion.getId())
                .orElseGet(() -> new ResumenInscripcion(inscripcion, nombreArchivo(inscripcion.getId())));
    }

    private ResumenArchivoResponse respuesta(Long inscripcionId, String bucket, String key, String rutaLocal) {
        return new ResumenArchivoResponse(inscripcionId, bucket, key, nombreArchivo(inscripcionId), rutaLocal);
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
