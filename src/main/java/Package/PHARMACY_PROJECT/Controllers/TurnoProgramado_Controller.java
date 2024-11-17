package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.TurnoProgramado_Model;
import Package.PHARMACY_PROJECT.Models.Horario_Model;
import Package.PHARMACY_PROJECT.Response;
import Package.PHARMACY_PROJECT.Services.Empleado_Services;
import Package.PHARMACY_PROJECT.Services.Horario_Services;
import Package.PHARMACY_PROJECT.Services.TurnoProgramado_Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/turnoProgramado")
public class TurnoProgramado_Controller {

    private static final Logger logger = LoggerFactory.getLogger(TurnoProgramado_Model.class);

    @Autowired
    private TurnoProgramado_Services turnoProgramadoService;

    @Autowired
    private Empleado_Services empleadoServices;

    @Autowired
    private Horario_Services horarioServices;




    // Endpoint para listar todos los horarios
    @GetMapping("/horarios")
    public ResponseEntity<Response<List<Horario_Model>>> listarHorarios() {
        try {
            List<Horario_Model> horarios = turnoProgramadoService.getAllHorarios();
            Response<List<Horario_Model>> response = new Response<>("200", "Horarios recuperados satisfactoriamente", horarios, "HORARIOS_FOUND");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<List<Horario_Model>> response = new Response<>("500", "Error al recuperar horarios", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint para asignar turnos programados
    @PostMapping("/asignar/{identificacion}")
    public ResponseEntity<Response<TurnoProgramado_Model>> asignarTurno(
            @PathVariable String identificacion, @RequestBody TurnoProgramado_Model turnoProgramadoModel) {
        try {
            // Buscar el empleado por identificación
            Optional<Empleado_Model> empleadoOpt = empleadoServices.findByIdentificacion(identificacion);

            if (!empleadoOpt.isPresent()) {
                // Si el empleado no se encuentra, retornar error 404
                Response<TurnoProgramado_Model> response = new Response<>(
                        "404", "Empleado no encontrado con identificación: " + identificacion, null, "EMPLEADO_NOT_FOUND"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Obtener el empleado
            Empleado_Model empleado = empleadoOpt.get();
            logger.info("Empleado encontrado: {} con identificación: {}", empleado.getNombre(), empleado.getIdentificacion());

            // Asignar el empleado al turno programado
            turnoProgramadoModel.setEmpleado(empleado);

            // Guardar el turno programado
            TurnoProgramado_Model turnoGuardado = turnoProgramadoService.saveTurnoProgramado(turnoProgramadoModel);

            // Responder con éxito
            Response<TurnoProgramado_Model> response = new Response<>(
                    "200", "Turno asignado correctamente", turnoGuardado, "TURN_ASSIGNED"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            // Registrar error y devolver mensaje genérico
            logger.error("Error al asignar turno al empleado: {}", e.getMessage(), e);
            Response<TurnoProgramado_Model> response = new Response<>(
                    "500", "Error al asignar turno: " + e.getMessage(), null, "INTERNAL_SERVER_ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // Endpoint para listar todos los turnos programados
    @GetMapping("/turnos")
    public ResponseEntity<Response<List<TurnoProgramado_Model>>> listarTurnos() {
        try {
            List<TurnoProgramado_Model> turnos = turnoProgramadoService.getAllTurnosProgramados();
            Response<List<TurnoProgramado_Model>> response = new Response<>("200", "Turnos programados recuperados satisfactoriamente", turnos, "TURNOS_FOUND");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<List<TurnoProgramado_Model>> response = new Response<>("500", "Error al recuperar turnos programados", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
