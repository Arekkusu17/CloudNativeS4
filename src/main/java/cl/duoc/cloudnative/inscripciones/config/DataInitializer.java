package cl.duoc.cloudnative.inscripciones.config;

import cl.duoc.cloudnative.inscripciones.model.Curso;
import cl.duoc.cloudnative.inscripciones.repository.CursoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarCursosIniciales(CursoRepository cursoRepository) {
        return args -> {
            if (cursoRepository.count() > 0) {
                return;
            }

            cursoRepository.saveAll(List.of(
                    new Curso("Desarrollo Cloud Native", "Camila Rojas", 32, new BigDecimal("129990")),
                    new Curso("Spring Boot para Microservicios", "Felipe Munoz", 24, new BigDecimal("99990")),
                    new Curso("CI/CD con GitHub Actions", "Valentina Soto", 18, new BigDecimal("79990"))
            ));
        };
    }
}
