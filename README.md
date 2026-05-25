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
