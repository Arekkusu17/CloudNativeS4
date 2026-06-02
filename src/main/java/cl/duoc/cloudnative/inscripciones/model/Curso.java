package cl.duoc.cloudnative.inscripciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "cursos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Setter
    @Column(name = "instructor", nullable = false, length = 120)
    private String instructor;

    @Setter
    @Column(name = "duracion_horas", nullable = false)
    private Integer duracionHoras;

    @Setter
    @Column(name = "costo", nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    public Curso(String nombre, String instructor, Integer duracionHoras, BigDecimal costo) {
        this.nombre = nombre;
        this.instructor = instructor;
        this.duracionHoras = duracionHoras;
        this.costo = costo;
    }
}
