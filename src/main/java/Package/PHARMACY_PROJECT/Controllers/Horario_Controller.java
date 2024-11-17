package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Horario_Model;
import Package.PHARMACY_PROJECT.Services.Horario_Services;
import Package.PHARMACY_PROJECT.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/horarios")
public class Horario_Controller {

    @Autowired
    private Horario_Services horarioServices;

    // Endpoint para guardar un horario
    @PostMapping("/guardar")
    public ResponseEntity<Response<Horario_Model>> saveHorario(@RequestBody Horario_Model horario) {
        try {
            Horario_Model savedHorario = horarioServices.saveHorario(horario);
            Response<Horario_Model> response = new Response<>("200", "Horario guardado exitosamente", savedHorario, "HORARIO_SAVED");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Response<Horario_Model> response = new Response<>("500", "Error al guardar horario: " + e.getMessage(), null, "ERROR_SAVING_HORARIO");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint para obtener todos los horarios
    @GetMapping("/listar")
    public ResponseEntity<Response<List<Horario_Model>>> getAllHorarios() {
        try {
            List<Horario_Model> horarios = horarioServices.getAllHorarios();
            Response<List<Horario_Model>> response = new Response<>("200", "Lista de horarios obtenida exitosamente", horarios, "HORARIOS_LIST");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<List<Horario_Model>> response = new Response<>("500", "Error al obtener lista de horarios: " + e.getMessage(), null, "ERROR_LISTING_HORARIOS");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint para obtener un horario por ID
    @GetMapping("/obtener/{id}")
    public ResponseEntity<Response<Horario_Model>> getHorarioById(@PathVariable Long id) {
        try {
            Optional<Horario_Model> horarioOpt = horarioServices.getHorarioById(id);
            if (horarioOpt.isPresent()) {
                Response<Horario_Model> response = new Response<>("200", "Horario encontrado", horarioOpt.get(), "HORARIO_FOUND");
                return ResponseEntity.ok(response);
            } else {
                Response<Horario_Model> response = new Response<>("404", "Horario no encontrado", null, "HORARIO_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Response<Horario_Model> response = new Response<>("500", "Error al buscar horario: " + e.getMessage(), null, "ERROR_FINDING_HORARIO");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint para eliminar un horario por ID
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Response<Void>> deleteHorario(@PathVariable Long id) {
        try {
            horarioServices.deleteHorario(id);
            Response<Void> response = new Response<>("200", "Horario eliminado exitosamente", null, "HORARIO_DELETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<Void> response = new Response<>("500", "Error al eliminar horario: " + e.getMessage(), null, "ERROR_DELETING_HORARIO");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Endpoint para buscar horarios por bloques de tiempo
    @GetMapping("/buscarPorBloques")
    public ResponseEntity<Response<List<Horario_Model>>> getHorariosByBloques(@RequestParam LocalTime horaInicio, @RequestParam LocalTime horaFin) {
        try {
            List<Horario_Model> horarios = horarioServices.getHorariosByBloques(horaInicio, horaFin);
            Response<List<Horario_Model>> response = new Response<>("200", "Horarios encontrados por bloques", horarios, "HORARIOS_BY_BLOQUES");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<List<Horario_Model>> response = new Response<>("500", "Error al buscar horarios por bloques: " + e.getMessage(), null, "ERROR_FINDING_BY_BLOQUES");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
