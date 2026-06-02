package cl.duoc.cloudnative.inscripciones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumenes_inscripcion")
public class ResumenInscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscripcion_id", nullable = false, unique = true)
    private Inscripcion inscripcion;

    @Column(name = "nombre_archivo", nullable = false, length = 160)
    private String nombreArchivo;

    @Column(name = "ruta_local", length = 500)
    private String rutaLocal;

    @Column(name = "bucket_s3", length = 120)
    private String bucketS3;

    @Column(name = "key_s3", length = 500)
    private String keyS3;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_subida_s3")
    private LocalDateTime fechaSubidaS3;

    protected ResumenInscripcion() {
    }

    public ResumenInscripcion(Inscripcion inscripcion, String nombreArchivo) {
        this.inscripcion = inscripcion;
        this.nombreArchivo = nombreArchivo;
    }

    @PrePersist
    void asignarFechaCreacion() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getRutaLocal() {
        return rutaLocal;
    }

    public String getBucketS3() {
        return bucketS3;
    }

    public String getKeyS3() {
        return keyS3;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaSubidaS3() {
        return fechaSubidaS3;
    }

    public void actualizarArchivoLocal(String nombreArchivo, String rutaLocal) {
        this.nombreArchivo = nombreArchivo;
        this.rutaLocal = rutaLocal;
    }

    public void actualizarS3(String bucketS3, String keyS3) {
        this.bucketS3 = bucketS3;
        this.keyS3 = keyS3;
        this.fechaSubidaS3 = LocalDateTime.now();
    }

    public void limpiarS3() {
        this.bucketS3 = null;
        this.keyS3 = null;
        this.fechaSubidaS3 = null;
    }
}
