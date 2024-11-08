package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Dueno_Model;
import Package.PHARMACY_PROJECT.Services.Dueno_Services;
import Package.PHARMACY_PROJECT.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/dueno")
public class Dueno_Controller {

        @Autowired
        private Dueno_Services duenoServices;

        @PostMapping
        public ResponseEntity<Response<Dueno_Model>> saveDueno(@RequestBody Dueno_Model dueno) {
            try {
                Dueno_Model duenoGuardado = duenoServices.save(dueno);
                Response<Dueno_Model> response = new Response<>("200", "Dueño registrado satisfactoriamente", duenoGuardado, "DUENO_CREATED");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } catch (Exception e) {
                Response<Dueno_Model> response = new Response<>("500", "Error al registrar al dueño", null, "INTERNAL_SERVER_ERROR");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }



        @DeleteMapping("/{id}")
        public ResponseEntity<Response<Void>> deleteDueno(@PathVariable Long id) {
            try {
                duenoServices.deleteById(id);
                Response<Void> response = new Response<>("200", "Dueño eliminado satisfactoriamente", null, "DUENO_DELETED");
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                Response<Void> response = new Response<>("500", "Error al eliminar al dueño", null, "INTERNAL_SERVER_ERROR");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
    }