package Package.PHARMACY_PROJECT.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "asistencias")
@Getter
@Setter
public class Asistencia_Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID autoincremental de la asistencia

    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado_Model empleado; // Relación con la tabla Empleados

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha; // Fecha y hora del registro de asistencia

    @Column(name = "hora_entrada", nullable = false)
    private LocalDateTime horaEntrada; // Hora de entrada

    @Column(name = "hora_salida")
    private LocalDateTime horaSalida; // Hora de salida (si corresponde)

    @Column(name = "estado", nullable = false, length = 20)
    private String estado; // Estado de la asistencia (presente, tarde, ausente)

    // Constructor vacío
    public Asistencia_Model() {}

    // Constructor con parámetros
    public Asistencia_Model(Empleado_Model empleado, LocalDateTime fecha, LocalDateTime horaEntrada, LocalDateTime horaSalida, String estado) {
        this.empleado = empleado;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.estado = estado;
    }}