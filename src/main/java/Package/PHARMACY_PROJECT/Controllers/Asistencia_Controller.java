package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Services.Asistencia_Services;
import Package.PHARMACY_PROJECT.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/asistencia")
public class Asistencia_Controller {

    @Autowired
    private Asistencia_Services asistenciaServices;

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
