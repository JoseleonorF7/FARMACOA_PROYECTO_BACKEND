package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Services.Empleado_Services;
import Package.PHARMACY_PROJECT.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/empleado")
public class Empleado_Controller {

    private static final Logger logger = LoggerFactory.getLogger(Empleado_Controller.class);

    @Autowired
    private Empleado_Services empleadoServices;

    // Método para obtener todas las huellas dactilares
    @GetMapping("/huellas")
    public ResponseEntity<Response<List<String>>> getAllHuellas() {
        try {
            List<String> huellas = empleadoServices.getAllHuellas();
            Response<List<String>> response = new Response<>("200", "Huellas recuperadas satisfactoriamente", huellas, "HUELLAS_FOUND");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al recuperar las huellas dactilares: ", e);
            Response<List<String>> response = new Response<>("500", "Error al recuperar las huellas dactilares", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Método para guardar empleado solo con huella dactilar
    @PostMapping("/registrarHuella")
    public ResponseEntity<Response<Empleado_Model>> saveEmpleadoHuella(@RequestBody Empleado_Model empleado) {
        try {
            // Aquí se guarda únicamente la huella
            Empleado_Model empleadoGuardado = empleadoServices.saveHuella(empleado);
            Response<Empleado_Model> response = new Response<>("200", "Huella registrada satisfactoriamente", empleadoGuardado, "HUELLA_CREATED");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            // Logueamos el error completo para ver el stack trace
            logger.error("Error al registrar la huella: ", e);

            // También puedes devolver el mensaje de error detallado en la respuesta para depuración
            Response<Empleado_Model> response = new Response<>("500", "Error al registrar la huella: " + e.getMessage(), null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // Método para mostrar la huella dactilar de un empleado
    @GetMapping("/huella/{id}")
    public ResponseEntity<Response<String>> getHuella(@PathVariable Long id) {
        try {
            // Recuperamos la huella dactilar del empleado por su ID
            String huella = empleadoServices.getHuellaById(id);
            if (huella != null) {
                Response<String> response = new Response<>("200", "Huella encontrada", huella, "HUELLA_FOUND");
                return ResponseEntity.ok(response);
            } else {
                Response<String> response = new Response<>("404", "Empleado no encontrado o huella no registrada", null, "HUELLA_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Response<String> response = new Response<>("500", "Error al recuperar la huella", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/registrar/{huella}")
    public ResponseEntity<Response<Empleado_Model>> updateEmpleado(
            @PathVariable String huella, @RequestBody Empleado_Model empleadoData) {
        try {
            // Buscar empleado por huella dactilar
            Optional<Empleado_Model> empleadoExistente = empleadoServices.findByHuellaDactilar(huella);

            if (empleadoExistente.isPresent()) {
                // Actualizar los datos del empleado existente
                Empleado_Model empleado = empleadoExistente.get();
                empleado.setNombre(empleadoData.getNombre());
                empleado.setIdentificacion(empleadoData.getIdentificacion());
                empleado.setFechaContratacion(empleadoData.getFechaContratacion());
                empleado.setActivo(empleadoData.getActivo());

                Empleado_Model empleadoGuardado = empleadoServices.save(empleado);

                Response<Empleado_Model> response = new Response<>("200", "Empleado actualizado satisfactoriamente", empleadoGuardado, "EMPLEADO_UPDATED");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                // La huella no fue encontrada, retornar error
                Response<Empleado_Model> response = new Response<>("404", "Empleado no encontrado para la huella proporcionada", null, "EMPLEADO_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            // Loguear el error completo
            logger.error("Error al actualizar al empleado: ", e);

            Response<Empleado_Model> response = new Response<>("500", "Error al actualizar al empleado: " + e.getMessage(), null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // Método para obtener todos los empleados
    @GetMapping("/all")
    public ResponseEntity<Response<List<Empleado_Model>>> getAllEmpleados() {
        try {
            List<Empleado_Model> empleados = empleadoServices.getAllEmpleados();
            Response<List<Empleado_Model>> response = new Response<>("200", "Empleados recuperados satisfactoriamente", empleados, "EMPLEADOS_FOUND");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al recuperar la lista de empleados: ", e);
            Response<List<Empleado_Model>> response = new Response<>("500", "Error al recuperar la lista de empleados", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/actualizar/{identificacion}")
    public ResponseEntity<Response<Empleado_Model>> updateEmpleadoByIdentificacion(
            @PathVariable String identificacion, @RequestBody Empleado_Model empleadoData) {
        try {
            // Buscar empleado por identificación
            Optional<Empleado_Model> empleadoExistente = empleadoServices.findByIdentificacion(identificacion);

            if (empleadoExistente.isPresent()) {
                Empleado_Model empleado = empleadoExistente.get();

                // Actualizar únicamente los campos específicos
                if (empleadoData.getFechaContratacion() != null) {
                    empleado.setFechaContratacion(empleadoData.getFechaContratacion());
                }
                if (empleadoData.getActivo() != null) {
                    empleado.setActivo(empleadoData.getActivo());
                }

                Empleado_Model empleadoGuardado = empleadoServices.save(empleado);

                Response<Empleado_Model> response = new Response<>("200", "Empleado actualizado satisfactoriamente", empleadoGuardado, "EMPLEADO_UPDATED");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                Response<Empleado_Model> response = new Response<>("404", "Empleado no encontrado para la identificación proporcionada", null, "EMPLEADO_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error al actualizar al empleado: ", e);

            Response<Empleado_Model> response = new Response<>("500", "Error al actualizar al empleado: " + e.getMessage(), null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // Método para eliminar un empleado por identificación
    @DeleteMapping("/eliminar/{identificacion}")
    public ResponseEntity<Response<Void>> deleteEmpleadoByIdentificacion(@PathVariable String identificacion) {
        try {
            boolean eliminado = empleadoServices.deleteByIdentificacion(identificacion);

            if (eliminado) {
                Response<Void> response = new Response<>("200", "Empleado eliminado satisfactoriamente", null, "EMPLEADO_DELETED");
                return ResponseEntity.ok(response);
            } else {
                Response<Void> response = new Response<>("404", "Empleado no encontrado para la identificación proporcionada", null, "EMPLEADO_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error al eliminar al empleado: ", e);

            Response<Void> response = new Response<>("500", "Error al eliminar al empleado", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Método para eliminar todos los empleados
    @DeleteMapping("/eliminar/todos")
    public ResponseEntity<Response<Void>> deleteAllEmpleados() {
        try {
            empleadoServices.deleteAll();

            Response<Void> response = new Response<>("200", "Todos los empleados eliminados satisfactoriamente", null, "ALL_EMPLEADOS_DELETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al eliminar todos los empleados: ", e);

            Response<Void> response = new Response<>("500", "Error al eliminar todos los empleados", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



}