package Package.PHARMACY_PROJECT.Models;

import Package.PHARMACY_PROJECT.Controllers.Asistencia_Controller;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static Package.PHARMACY_PROJECT.Controllers.Asistencia_Controller.*;

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
    private LocalDate fecha; // Fecha del registro de asistencia

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada; // Hora de entrada

    @Column(name = "hora_salida")
    private LocalTime horaSalida; // Hora de salida (si corresponde)

    @Column(name = "estado", nullable = false, length = 20)
    private String estado; // Estado de la asistencia (temprano, puntual, tarde)

    @Column(name = "tipoRegistro", nullable = false, length = 20)
    private String tipoRegistro; // Estado de la asistencia (temprano, puntual, tarde)

    // Constructor vacío
    public Asistencia_Model() {}

    // Constructor para registrar entrada
    public Asistencia_Model(Empleado_Model empleado, LocalDate fecha, LocalTime horaEntrada,LocalTime horaSalida, String estado,String tipoRegistro) {
        this.empleado = empleado;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.estado = estado;
        this.horaSalida = horaSalida; // Salida inicial es null
        this.tipoRegistro =tipoRegistro;
    }

    // Getters y Setters
    // ...

    // Método en el modelo para actualizar el estado de salida
    public void actualizarEstadoSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;

        // Calcular diferencia en minutos para salida con la hora de referencia de salida
        long diferenciaMinutos = ChronoUnit.MINUTES.between(HORA_REFERENCIA_SALIDA, horaSalida);

        if (diferenciaMinutos < RANGO_TEMPRANO) {
            this.estado = "temprano";
        } else if (diferenciaMinutos > RANGO_TARDE) {
            this.estado = "tarde";
        } else {
            this.estado = "puntual";
        }
}}