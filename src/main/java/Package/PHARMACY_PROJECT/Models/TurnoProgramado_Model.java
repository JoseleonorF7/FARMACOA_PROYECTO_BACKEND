package Package.PHARMACY_PROJECT.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "turnos_programados")
@Getter
@Setter
public class TurnoProgramado_Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ID autoincremental

    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado_Model empleado;  // Empleado asignado al turno

    @Column(name = "fecha", nullable = true)
    private LocalDate fecha;  // Fecha específica del turno

    @Column(name = "hora_inicio", nullable = true)
    private LocalTime horaInicio;  // Hora de inicio específica (puede coincidir con el horario)

    @Column(name = "hora_fin", nullable = true)
    private LocalTime horaFin;  // Hora de fin específica (puede coincidir con el horario)

    // Constructor vacío
    public TurnoProgramado_Model() {
    }



    public TurnoProgramado_Model(Empleado_Model empleado, LocalTime horaFin, LocalDate fecha, LocalTime horaInicio) {
        this.empleado = empleado;
        this.horaFin = horaFin;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
    }
}