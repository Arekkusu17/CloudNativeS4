package cl.duoc.cloudnative.inscripciones.controller;

import cl.duoc.cloudnative.inscripciones.dto.InscripcionRequest;
import cl.duoc.cloudnative.inscripciones.dto.InscripcionResponse;
import cl.duoc.cloudnative.inscripciones.dto.ResumenArchivoResponse;
import cl.duoc.cloudnative.inscripciones.service.InscripcionService;
import cl.duoc.cloudnative.inscripciones.service.ResumenInscripcionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;
    private final ResumenInscripcionService resumenInscripcionService;

    public InscripcionController(
            InscripcionService inscripcionService,
            ResumenInscripcionService resumenInscripcionService) {
        this.inscripcionService = inscripcionService;
        this.resumenInscripcionService = resumenInscripcionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InscripcionResponse crearInscripcion(@RequestBody InscripcionRequest request) {
        return inscripcionService.crearInscripcion(request);
    }

    @GetMapping
    public List<InscripcionResponse> listarInscripciones() {
        return inscripcionService.listarInscripciones();
    }

    @GetMapping("/{id}/resumen")
    public ResponseEntity<byte[]> descargarResumenGenerado(@PathVariable Long id) {
        byte[] contenido = resumenInscripcionService.descargarResumenLocal(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resumenInscripcionService.nombreArchivo(id) + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(contenido);
    }

    @PostMapping("/{id}/resumen/s3")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumenArchivoResponse subirResumenGenerado(@PathVariable Long id) {
        return resumenInscripcionService.subirResumenGenerado(id);
    }

    @PutMapping("/{id}/resumen/s3")
    public ResumenArchivoResponse modificarResumenEnS3(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return resumenInscripcionService.reemplazarResumen(id, file);
    }

    @GetMapping("/{id}/resumen/s3")
    public ResponseEntity<byte[]> descargarResumenDesdeS3(@PathVariable Long id) {
        byte[] contenido = resumenInscripcionService.descargarResumenDesdeS3(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resumenInscripcionService.nombreArchivo(id) + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(contenido);
    }

    @DeleteMapping("/{id}/resumen/s3")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrarResumenEnS3(@PathVariable Long id) {
        resumenInscripcionService.borrarResumen(id);
    }
}
