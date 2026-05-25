package cl.duoc.cloudnative.inscripciones.controller;

import cl.duoc.cloudnative.inscripciones.dto.InscripcionRequest;
import cl.duoc.cloudnative.inscripciones.dto.InscripcionResponse;
import cl.duoc.cloudnative.inscripciones.service.InscripcionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
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
}
