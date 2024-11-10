package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Services.Asistencia_Services;
import Package.PHARMACY_PROJECT.Response;
import Package.PHARMACY_PROJECT.Services.Empleado_Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/asistencia")
public class Asistencia_Controller {

    public static final int RANGO_TEMPRANO = -10; // 10 minutos antes
    public static final int RANGO_TARDE = 10;     // 10 minutos después
    public static final LocalTime HORA_REFERENCIA_ENTRADA = LocalTime.of(7, 0); // 7 am para entrada
    public static final LocalTime HORA_REFERENCIA_SALIDA = LocalTime.of(19, 0); // 7 pm para salida

    @Autowired
    private Asistencia_Services asistenciaServices;

    @Autowired
    private Empleado_Services empleadoServices;


    private static final Logger logger = LoggerFactory.getLogger(Asistencia_Controller.class);

    // Método para registrar la entrada del empleado
    // Método para registrar la entrada del empleado
    @PostMapping("/entrada/{huella}")
    public ResponseEntity<Response<Asistencia_Model>> registrarEntrada(@PathVariable String huella) {
            // Buscar el empleado por huella dactilar
            Optional<Empleado_Model> empleadoOptional = empleadoServices.findByHuellaDactilar(huella);
            if (!empleadoOptional.isPresent()) {
                logger.error("Empleado no encontrado para la huella: " + huella);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("404", "Empleado no encontrado", null, "EMPLEADO_NO_ENCONTRADO"));
            }

            Empleado_Model empleado = empleadoOptional.get();

            // Verificar si el empleado está activo
            if (!empleado.isActivo()) {
                logger.error("Empleado no activo: " + empleado.getNombre());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "Empleado no activo", null, "EMPLEADO_INACTIVO"));
            }

            // Obtener la fecha actual
            LocalDate fechaActual = LocalDate.now();

            // Verificar si ya existe una asistencia para hoy (no permitir múltiples entradas)
            Optional<Asistencia_Model> asistenciaOptional = asistenciaServices.findByEmpleadoAndFechaAndTipoRegistro(empleado, fechaActual,"ENTRADA");
            if (asistenciaOptional.isPresent()) {
                logger.error("El empleado ya tiene una entrada registrada para hoy");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "Ya se registró una entrada para hoy", null, "ENTRADA_DUPLICADA"));
            }

            // Crear el registro de asistencia con horaSalida null
            Asistencia_Model asistencia = new Asistencia_Model(empleado, fechaActual, LocalTime.now(),null,calcularEstadoEntrada(LocalTime.now()),"ENTRADA");
            asistenciaServices.save(asistencia);
            logger.info("Entrada registrada para el empleado: " + empleado.getNombre());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new Response<>("200", "Entrada registrada", asistencia, "ENTRADA_REGISTRADA"));

    }

    @PostMapping("/salida/{huella}")
    public ResponseEntity<Response<Asistencia_Model>> registrarSalida(@PathVariable String huella) {

            Optional<Empleado_Model> empleadoOptional = empleadoServices.findByHuellaDactilar(huella);
            if (!empleadoOptional.isPresent()) {
                logger.error("Empleado no encontrado para la huella: " + huella);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("404", "Empleado no encontrado", null, "EMPLEADO_NO_ENCONTRADO"));
            }

            Empleado_Model empleado = empleadoOptional.get();

            if (!empleado.isActivo()) {
                logger.error("Empleado no activo: " + empleado.getNombre());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "Empleado no activo", null, "EMPLEADO_INACTIVO"));
            }

            LocalDate fechaActual = LocalDate.now();

            Optional<Asistencia_Model> asistenciaEntradaOptional = asistenciaServices.findByEmpleadoAndFechaAndTipoRegistro(empleado, fechaActual, "ENTRADA");
            if (!asistenciaEntradaOptional.isPresent()) {
                logger.error("No se encontró una entrada para hoy");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("404", "No se encontró una entrada registrada para hoy", null, "ASISTENCIA_NO_ENCONTRADA"));
            }

            Optional<Asistencia_Model> asistenciaSalidaOptional = asistenciaServices.findByEmpleadoAndFechaAndTipoRegistro(empleado, fechaActual, "SALIDA");
            if (asistenciaSalidaOptional.isPresent()) {
                logger.error("El empleado ya tiene una salida registrada para hoy");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "Ya se registró una salida para hoy", null, "SALIDA_DUPLICADA"));
            }

            Asistencia_Model asistenciaSalida = new Asistencia_Model(empleado, fechaActual, null, LocalTime.now(), calcularEstadoSalida(LocalTime.now()), "SALIDA");

            asistenciaServices.save(asistenciaSalida);
            logger.info("Salida registrada para el empleado: " + empleado.getNombre());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new Response<>("200", "Salida registrada", asistenciaSalida, "SALIDA_REGISTRADA"));


    }



    // Método para calcular el estado de la entrada
    public String calcularEstadoEntrada(LocalTime horaEntrada) {
        // Calcular la diferencia en minutos entre la hora de entrada y la hora de referencia
        long diferenciaMinutos = ChronoUnit.MINUTES.between(HORA_REFERENCIA_ENTRADA, horaEntrada);

        // Determinar el estado en función de la diferencia de minutos
        if (diferenciaMinutos < RANGO_TEMPRANO) {
            return "temprano";
        } else if (diferenciaMinutos > RANGO_TARDE) {
            return "tarde";
        } else {
            return "puntual";
        }
    }

    // Método para calcular el estado de la entrada
    public String calcularEstadoSalida(LocalTime horaSalida) {
        // Calcular la diferencia en minutos entre la hora de entrada y la hora de referencia
        long diferenciaMinutos = ChronoUnit.MINUTES.between(HORA_REFERENCIA_SALIDA, horaSalida);

        // Determinar el estado en función de la diferencia de minutos
        if (diferenciaMinutos < RANGO_TEMPRANO) {
            return "temprano";
        } else if (diferenciaMinutos > RANGO_TARDE) {
            return "tarde";
        } else {
            return "puntual";
        }
    }



    // Métodos existentes
    @PostMapping
    public ResponseEntity<Response<Asistencia_Model>> saveAsistencia(@RequestBody Asistencia_Model asistencia) {
        try {
            Asistencia_Model asistenciaGuardada = asistenciaServices.save(asistencia);
            Response<Asistencia_Model> response = new Response<>("200", "Asistencia registrada satisfactoriamente", asistenciaGuardada, "ASISTENCIA_CREATED");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Response<Asistencia_Model> response = new Response<>("500", "Error al registrar la asistencia", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteAsistencia(@PathVariable Long id) {
        try {
            asistenciaServices.deleteById(id);
            Response<Void> response = new Response<>("200", "Asistencia eliminada satisfactoriamente", null, "ASISTENCIA_DELETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<Void> response = new Response<>("500", "Error al eliminar la asistencia", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
