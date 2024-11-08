package Package.PHARMACY_PROJECT.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "informes_asistencia")
@Getter
@Setter
public class InformeAsistencia_Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID autoincremental del informe

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion; // Fecha de generación del informe

    @Column(name = "filtros_aplicados", nullable = false)
    private String filtrosAplicados; // Filtros aplicados al informe en formato JSON

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo; // Tipo de informe (diario, semanal, mensual)

    @Column(name = "archivo", length = 255)
    private String archivo; // Enlace al archivo PDF o Excel generado

    // Constructor vacío
    public InformeAsistencia_Model() {
    }

    // Constructor con parámetros
    public InformeAsistencia_Model(LocalDateTime fechaGeneracion, String filtrosAplicados, String tipo, String archivo) {
        this.fechaGeneracion = fechaGeneracion;
        this.filtrosAplicados = filtrosAplicados;
        this.tipo = tipo;
        this.archivo = archivo;
    }
}