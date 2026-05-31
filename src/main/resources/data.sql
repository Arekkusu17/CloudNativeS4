INSERT INTO cursos (nombre, instructor, duracion_horas, costo)
SELECT 'Desarrollo Cloud Native', 'Camila Rojas', 32, 129990.00
WHERE NOT EXISTS (SELECT 1 FROM cursos WHERE nombre = 'Desarrollo Cloud Native');

INSERT INTO cursos (nombre, instructor, duracion_horas, costo)
SELECT 'Spring Boot para Microservicios', 'Felipe Munoz', 24, 99990.00
WHERE NOT EXISTS (SELECT 1 FROM cursos WHERE nombre = 'Spring Boot para Microservicios');

INSERT INTO cursos (nombre, instructor, duracion_horas, costo)
SELECT 'CI/CD con GitHub Actions', 'Valentina Soto', 18, 79990.00
WHERE NOT EXISTS (SELECT 1 FROM cursos WHERE nombre = 'CI/CD con GitHub Actions');
