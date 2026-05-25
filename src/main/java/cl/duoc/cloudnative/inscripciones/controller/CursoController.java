package cl.duoc.cloudnative.inscripciones.controller;

import cl.duoc.cloudnative.inscripciones.dto.CursoRequest;
import cl.duoc.cloudnative.inscripciones.dto.CursoResponse;
import cl.duoc.cloudnative.inscripciones.service.CursoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public List<CursoResponse> listarCursos() {
        return cursoService.listarCursos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CursoResponse crearCurso(@RequestBody CursoRequest request) {
        return cursoService.crearCurso(request);
    }
}
