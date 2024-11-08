package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Services.Empleado_Services;
import Package.PHARMACY_PROJECT.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/empleado")
public class Empleado_Controller {

    @Autowired
    private Empleado_Services empleadoServices;

    @PostMapping
    public ResponseEntity<Response<Empleado_Model>> saveEmpleado(@RequestBody Empleado_Model empleado) {
        try {
            Empleado_Model empleadoGuardado = empleadoServices.save(empleado);
            Response<Empleado_Model> response = new Response<>("200", "Empleado registrado satisfactoriamente", empleadoGuardado, "EMPLEADO_CREATED");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Response<Empleado_Model> response = new Response<>("500", "Error al registrar al empleado", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteEmpleado(@PathVariable Long id) {
        try {
            empleadoServices.deleteById(id);
            Response<Void> response = new Response<>("200", "Empleado eliminado satisfactoriamente", null, "EMPLEADO_DELETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<Void> response = new Response<>("500", "Error al eliminar al empleado", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}