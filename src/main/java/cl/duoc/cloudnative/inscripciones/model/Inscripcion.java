package cl.duoc.cloudnative.inscripciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inscripciones")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String estudianteNombre;

    @Column(nullable = false)
    private String estudianteEmail;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "inscripcion_cursos",
            joinColumns = @JoinColumn(name = "inscripcion_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    private List<Curso> cursos = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private LocalDateTime fechaInscripcion;

    protected Inscripcion() {
    }

    public Inscripcion(String estudianteNombre, String estudianteEmail, List<Curso> cursos, BigDecimal total) {
        this.estudianteNombre = estudianteNombre;
        this.estudianteEmail = estudianteEmail;
        this.cursos = cursos;
        this.total = total;
        this.fechaInscripcion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEstudianteNombre() {
        return estudianteNombre;
    }

    public String getEstudianteEmail() {
        return estudianteEmail;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }
}
