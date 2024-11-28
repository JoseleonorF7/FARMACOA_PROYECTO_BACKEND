package Package.PHARMACY_PROJECT.Models;

import Package.PHARMACY_PROJECT.Controllers.Asistencia_Controller;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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

    @Transient
    private LocalTime horaEntrada2;

    @Transient
    private LocalTime horaSalida2;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado; // Estado de la asistencia (temprano, puntual, tarde)

    @Column(name = "tipoRegistro", nullable = false, length = 20)
    private String tipoRegistro; // Estado de la asistencia (temprano, puntual, tarde)

    @Transient
    private String diferenciaTiempoEntrada; // Diferencia de tiempo, no persistente en la base de datos

    @Transient
    private String diferenciaTiempoSalida;


    @Transient
    private String diferenciaTiempoEntrada2; // Diferencia de tiempo, no persistente en la base de datos

    @Transient
    private String diferenciaTiempoSalida2;


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
}

    public String calcularDiferenciaTiempoEntrada(LocalTime horaReferenciaEntrada1, LocalTime horaReferenciaEntrada2) {
        // Verifica si horaEntrada o horaEntrada2 están registradas y calcula en cada caso.
        if (horaEntrada != null) {
            return calcularDiferencia(horaReferenciaEntrada1, horaEntrada);
        } else if (horaEntrada2 != null) {
            return calcularDiferencia(horaReferenciaEntrada2, horaEntrada2);
        }
        return "No disponible";
    }

    public String calcularDiferenciaTiempoSalida(LocalTime horaReferenciaSalida1, LocalTime horaReferenciaSalida2) {
        // Verifica si horaSalida o horaSalida2 están registradas y calcula en cada caso.
        if (horaSalida != null) {
            return calcularDiferencia(horaReferenciaSalida1, horaSalida);
        } else if (horaSalida2 != null) {
            return calcularDiferencia(horaReferenciaSalida2, horaSalida2);
        }
        return "No disponible";
    }

    // Método privado para calcular la diferencia de tiempo
    private String calcularDiferencia(LocalTime referencia, LocalTime actual) {
        long diferencia = ChronoUnit.MINUTES.between(referencia, actual);

        if (diferencia < RANGO_TEMPRANO) { // Temprano
            long horasTemprano = Math.abs(diferencia) / 60;
            long minutosTemprano = Math.abs(diferencia) % 60;
            return horasTemprano > 0 ? "Temprano por " + horasTemprano + " hora(s) y " + minutosTemprano + " minuto(s)" : "Temprano por " + minutosTemprano + " minuto(s)";
        } else if (diferencia > RANGO_TARDE) { // Tarde
            long horasTarde = diferencia / 60;
            long minutosTarde = diferencia % 60;
            return horasTarde > 0 ? "Tarde por " + horasTarde + " hora(s) y " + minutosTarde + " minuto(s)" : "Tarde por " + minutosTarde + " minuto(s)";
        } else { // Puntual
            return "Puntual";
        }
    }

}

