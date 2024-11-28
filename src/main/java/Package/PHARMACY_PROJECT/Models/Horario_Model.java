package Package.PHARMACY_PROJECT.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Entity
@Table(name = "horarios")
@Getter
@Setter
public class Horario_Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ID autoincremental

    @Column(name = "descripcion", length = 100, nullable = true)
    private String descripcion;  // Descripción del horario (por ejemplo, "Turno Mañana")

    @Column(name = "hora_inicio_1", nullable = false)
    private LocalTime horaInicio1;  // Hora de inicio del primer bloque

    @Column(name = "hora_fin_1", nullable = false)
    private LocalTime horaFin1;  // Hora de fin del primer bloque

    @Column(name = "hora_inicio_2", nullable = true)
    private LocalTime horaInicio2;  // Hora de inicio del segundo bloque (si aplica)

    @Column(name = "hora_fin_2", nullable = true)
    private LocalTime horaFin2;  // Hora de fin del segundo bloque (si aplica)

    // Constructor vacío
    public Horario_Model() {
    }

    // Constructor con atributos
    public Horario_Model(String descripcion, LocalTime horaFin1, LocalTime horaInicio2, LocalTime horaInicio1, LocalTime horaFin2) {
        this.descripcion = descripcion;
        this.horaFin1 = horaFin1;
        this.horaInicio2 = horaInicio2;
        this.horaInicio1 = horaInicio1;
        this.horaFin2 = horaFin2;
    }
}