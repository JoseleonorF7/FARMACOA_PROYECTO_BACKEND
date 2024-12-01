package Package.PHARMACY_PROJECT;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class config implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:4200", // Para desarrollo local
                        "https://farmacia-proyecto-front.vercel.app" // Dominio desplegado en Vercel
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                .allowedHeaders("*") // Permitir todos los encabezados
                .exposedHeaders("Content-Disposition", "Authorization") // Exponer encabezados
                .allowCredentials(true); // Permitir envío de cookies o credenciales si es necesario
    }
}
