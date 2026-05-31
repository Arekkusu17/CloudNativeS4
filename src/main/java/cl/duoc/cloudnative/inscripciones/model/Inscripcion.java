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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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

    @Column(name = "estudiante_nombre", nullable = false, length = 120)
    private String estudianteNombre;

    @Column(name = "estudiante_email", nullable = false, length = 160)
    private String estudianteEmail;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "inscripcion_cursos",
            joinColumns = @JoinColumn(name = "inscripcion_id"),
            inverseJoinColumns = @JoinColumn(name = "curso_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_inscripcion_curso",
                    columnNames = {"inscripcion_id", "curso_id"}
            )
    )
    private List<Curso> cursos = new ArrayList<>();

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "fecha_inscripcion", nullable = false)
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

    @PrePersist
    void asignarFechaInscripcion() {
        if (fechaInscripcion == null) {
            fechaInscripcion = LocalDateTime.now();
        }
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
