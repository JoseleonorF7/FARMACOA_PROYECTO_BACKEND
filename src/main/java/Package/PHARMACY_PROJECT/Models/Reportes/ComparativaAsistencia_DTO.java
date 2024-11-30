package Package.PHARMACY_PROJECT.Models.Reportes;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Getter
@Setter
public class ComparativaAsistencia_DTO {


        private Long empleadoId;
        private String empleadoNombre;
        private int tardanzas;
        private int puntualidades;

    public ComparativaAsistencia_DTO(Long empleadoId, String empleadoNombre, int puntualidades, int tardanzas) {
        this.empleadoId = empleadoId;
        this.empleadoNombre = empleadoNombre;
        this.puntualidades = puntualidades;
        this.tardanzas = tardanzas;
    }

    public void incrementarPuntualidades() {
        this.puntualidades++;
    }

    public void incrementarTardanzas() {
        this.tardanzas++;
    }
        // Getters y Setters
    }


