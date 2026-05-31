# Curso Inscripciones Service

Microservicio Spring Boot para una plataforma educativa. Usa H2 como base de datos local y expone endpoints para cursos e inscripciones.

## Ejecutar localmente

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

La consola H2 queda disponible en `http://localhost:8080/h2-console`.

- JDBC URL: `jdbc:h2:file:./data/inscripciones-db`
- User: `sa`
- Password: dejar vacio

## Base de datos

El esquema de base de datos se construye desde `src/main/resources/schema.sql` y los datos iniciales desde `src/main/resources/data.sql`. La aplicacion no depende de `ddl-auto=update`, por lo que las tablas y relaciones quedan declaradas de forma explicita y reproducible.

### Tabla `cursos`

| Columna | Tipo | Descripcion |
| --- | --- | --- |
| `id` | `BIGINT` | Identificador primario autoincremental. |
| `nombre` | `VARCHAR(120)` | Nombre del curso. |
| `instructor` | `VARCHAR(120)` | Relator o instructor asignado. |
| `duracion_horas` | `INTEGER` | Duracion del curso. Debe ser mayor a cero. |
| `costo` | `DECIMAL(12,2)` | Valor del curso. Debe ser mayor o igual a cero. |

### Tabla `inscripciones`

| Columna | Tipo | Descripcion |
| --- | --- | --- |
| `id` | `BIGINT` | Identificador primario autoincremental. |
| `estudiante_nombre` | `VARCHAR(120)` | Nombre del estudiante inscrito. |
| `estudiante_email` | `VARCHAR(160)` | Correo del estudiante. |
| `total` | `DECIMAL(12,2)` | Suma de los costos de los cursos inscritos. |
| `fecha_inscripcion` | `TIMESTAMP` | Fecha y hora en que se registra la inscripcion. |

### Tabla `inscripcion_cursos`

Tabla intermedia que resuelve la relacion muchos a muchos entre inscripciones y cursos.

| Columna | Tipo | Descripcion |
| --- | --- | --- |
| `inscripcion_id` | `BIGINT` | Referencia a `inscripciones.id`. |
| `curso_id` | `BIGINT` | Referencia a `cursos.id`. |

## Endpoints

### Listar cursos

```http
GET /api/cursos
```

### Crear curso

```http
POST /api/cursos
Content-Type: application/json

{
  "nombre": "Arquitectura de Microservicios",
  "instructor": "Daniela Perez",
  "duracionHoras": 20,
  "costo": 89990
}
```

### Crear inscripcion

```http
POST /api/inscripciones
Content-Type: application/json

{
  "estudianteNombre": "Ana Gonzalez",
  "estudianteEmail": "ana.gonzalez@duocuc.cl",
  "cursoIds": [1, 2]
}
```

La respuesta incluye cursos seleccionados, costo por curso y total a pagar.

### Listar inscripciones

```http
GET /api/inscripciones
```

## Docker

Construir imagen:

```bash
docker build -t tu-usuario-dockerhub/curso-inscripciones-service:latest .
```

Ejecutar contenedor:

```bash
docker run -p 8080:8080 tu-usuario-dockerhub/curso-inscripciones-service:latest
```

Publicar en Docker Hub:

```bash
docker login
docker push tu-usuario-dockerhub/curso-inscripciones-service:latest
```
