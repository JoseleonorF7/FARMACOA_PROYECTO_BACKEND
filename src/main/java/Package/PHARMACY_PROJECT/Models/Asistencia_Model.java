package Package.PHARMACY_PROJECT.Models;

import Package.PHARMACY_PROJECT.Controllers.Asistencia_Controller;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(Asistencia_Model.class);

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


    @Transient
    private LocalTime horaEntrada2;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado; // Estado de la asistencia (temprano, puntual, tarde)

    @Column(name = "tipoRegistro", nullable = false, length = 20)
    private String tipoRegistro; // Estado de la asistencia (temprano, puntual, tarde)

    @Column(name = "diferencia_tiempo_entrada", nullable = true)
    private String diferenciaTiempoEntrada; // Diferencia de tiempo, no persistente en la base de datos


    // Constructor vacío
    public Asistencia_Model() {}

    // Constructor para registrar entrada
    public Asistencia_Model(Empleado_Model empleado, LocalDate fecha, LocalTime horaEntrada, String estado,String tipoRegistro) {
        this.empleado = empleado;
        this.fecha = fecha;
        this.horaEntrada = horaEntrada;
        this.estado = estado;
        this.tipoRegistro =tipoRegistro;
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


    // Método privado para calcular la diferencia de tiempo
    private String calcularDiferencia(LocalTime referencia, LocalTime actual) {
        // Obtener la diferencia total en segundos (sin valor absoluto)
        long diferenciaSegundos = ChronoUnit.SECONDS.between(referencia, actual);

        // Convertir la diferencia en horas, minutos y segundos
        long horas = Math.abs(diferenciaSegundos) / 3600;
        long minutos = (Math.abs(diferenciaSegundos) % 3600) / 60;
        long segundos = Math.abs(diferenciaSegundos) % 60;

        if (diferenciaSegundos < RANGO_TEMPRANO * 60) { // Temprano (conversión de minutos a segundos)
            return horas > 0 ? "Temprano por " + horas + " hora(s), " + minutos + " minuto(s) y " + segundos + " segundo(s)"
                    : minutos > 0 ? "Temprano por " + minutos + " minuto(s) y " + segundos + " segundo(s)"
                    : "Temprano por " + segundos + " segundo(s)";
        } else if (diferenciaSegundos > RANGO_TARDE * 60) { // Tarde
            return horas > 0 ? "Tarde por " + horas + " hora(s), " + minutos + " minuto(s) y " + segundos + " segundo(s)"
                    : minutos > 0 ? "Tarde por " + minutos + " minuto(s) y " + segundos + " segundo(s)"
                    : "Tarde por " + segundos + " segundo(s)";
        } else { // Puntual
            return "Puntual";
        }
    }




}

