package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.Horario_Model;
import Package.PHARMACY_PROJECT.Services.Empleado_Services;
import Package.PHARMACY_PROJECT.Response;
import Package.PHARMACY_PROJECT.Services.Horario_Services;
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

    @Autowired
    private Horario_Services horarioServices;

    @GetMapping("/huellas")
    public ResponseEntity<Response<List<String>>> getAllHuellas() {
        try {
            // Obtener solo las huellas donde la identificación es null o está vacía
            List<String> huellas = empleadoServices.getHuellasSinIdentificacion();
            Response<List<String>> response = new Response<>("200", "Huellas recuperadas satisfactoriamente", huellas, "HUELLAS_FOUND");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al recuperar las huellas dactilares: ", e);
            Response<List<String>> response = new Response<>("500", "Error al recuperar las huellas dactilares", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // Método para obtener todos los empleados
    @GetMapping("/all")
    public ResponseEntity<Response<List<Empleado_Model>>> getAllEmpleados() {
        try {
            List<Empleado_Model> empleados = empleadoServices.findAll();
            Response<List<Empleado_Model>> response = new Response<>("200", "Empleados recuperados satisfactoriamente", empleados, "EMPLEADOS_FOUND");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al recuperar la lista de empleados: ", e);
            Response<List<Empleado_Model>> response = new Response<>("500", "Error al recuperar la lista de empleados", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Método para obtener un empleado por su identificación
    @GetMapping("/identificacion/{identificacion}")
    public ResponseEntity<Response<Empleado_Model>> getEmpleadoByIdentificacion(@PathVariable String identificacion) {
        try {
            // Buscar al empleado por su identificación
            Optional<Empleado_Model> empleado = empleadoServices.findByIdentificacion(identificacion);

            if (empleado.isPresent()) {
                Response<Empleado_Model> response = new Response<>("200", "Empleado encontrado", empleado.get(), "EMPLEADO_FOUND");
                return ResponseEntity.ok(response);
            } else {
                Response<Empleado_Model> response = new Response<>("404", "Empleado no encontrado con la identificación proporcionada", null, "EMPLEADO_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error al recuperar el empleado por identificación: ", e);
            Response<Empleado_Model> response = new Response<>("500", "Error al recuperar el empleado", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Método para guardar empleado solo con huella dactilar
    @PostMapping("/registrarHuella")
    public ResponseEntity<Response<Empleado_Model>> saveEmpleadoHuella(@RequestBody Empleado_Model empleado) {
        try {
            // Verificar si la huella dactilar ya está registrada
            Optional<Empleado_Model> empleadoExistente = empleadoServices.findByHuellaDactilar(empleado.getHuellaDactilar());

            if (empleadoExistente.isPresent()) {
                // Si la huella ya existe, retornar un error
                Response<Empleado_Model> response = new Response<>("400", "La huella dactilar ya está registrada", null, "HUELLA_DUPLICATE");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Guardar el nuevo empleado con la huella
            Empleado_Model empleadoGuardado = empleadoServices.saveHuella(empleado);
            Response<Empleado_Model> response = new Response<>("200", "Huella registrada satisfactoriamente", empleadoGuardado, "HUELLA_CREATED");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            // Loguear el error completo
            logger.error("Error al registrar la huella: ", e);

            // Respuesta de error detallada
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
            // Verificar si ya existe un empleado con la misma identificación
            Optional<Empleado_Model> empleadoConIdentificacion = empleadoServices.findByIdentificacion(empleadoData.getIdentificacion());


            if (empleadoConIdentificacion.isPresent()) {
                // Si el empleado ya existe con esa identificación, retornar un mensaje
                Response<Empleado_Model> response = new Response<>("400", "Empleado con esa identificación ya existe", null, "EMPLEADO_DUPLICADO");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Buscar empleado por huella dactilar
            Optional<Empleado_Model> empleadoExistente = empleadoServices.findByHuellaDactilar(huella);

            if (empleadoExistente.isPresent()) {
                // Actualizar los datos del empleado existente
                Empleado_Model empleado = empleadoExistente.get();
                empleado.setNombre(empleadoData.getNombre());
                empleado.setIdentificacion(empleadoData.getIdentificacion());
                empleado.setFechaContratacion(empleadoData.getFechaContratacion());
                empleado.setActivo(empleadoData.getActivo());
                empleado.setRol(empleadoData.getRol());
                empleado.setHorario(empleadoData.getHorario());
                empleado.setTurnoProgramado(empleadoData.getTurnoProgramado());

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


    @PutMapping("/actualizar/{identificacion}")
    public ResponseEntity<Response<Empleado_Model>> updateEmpleadoByIdentificacion(

            @PathVariable String identificacion, @RequestBody Empleado_Model empleadoData) {
        try {
            // Buscar empleado por identificación
            Optional<Empleado_Model> empleadoExistente = empleadoServices.findByIdentificacion(identificacion);

            if (empleadoExistente.isPresent()) {
                Empleado_Model empleado = empleadoExistente.get();

                // Verificar si los datos proporcionados son iguales a los existentes
                boolean cambiosRealizados = false;

                if (empleadoData.getFechaContratacion() != null
                        && !empleadoData.getFechaContratacion().equals(empleado.getFechaContratacion())) {
                    empleado.setFechaContratacion(empleadoData.getFechaContratacion());
                    cambiosRealizados = true;
                }
                if (empleadoData.getActivo() != null
                        && !empleadoData.getActivo().equals(empleado.getActivo())) {
                    empleado.setActivo(empleadoData.getActivo());
                    cambiosRealizados = true;
                }
                if (empleadoData.getRol() != null
                        && !empleadoData.getRol().equals(empleado.getRol())) {
                    empleado.setRol(empleadoData.getRol());
                    cambiosRealizados = true;
                }
                if (empleadoData.getHorario() != null && !empleadoData.getHorario().equals(empleado.getHorario())) {
                    // Validar que el horario existe
                    Optional<Horario_Model> horarioExistente = horarioServices.getHorarioById(empleadoData.getHorario().getId());
                    if (horarioExistente.isPresent()) {
                        empleado.setHorario(horarioExistente.get());
                        cambiosRealizados = true;
                    } else {
                        Response<Empleado_Model> response = new Response<>(
                                "404",
                                "Horario no encontrado para el ID proporcionado",
                                null,
                                "HORARIO_NOT_FOUND"
                        );
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    }
                }


                // Si no se realizaron cambios, devolver respuesta adecuada
                if (!cambiosRealizados) {
                    Response<Empleado_Model> response = new Response<>(
                            "200",
                            "NO HUBO CAMBIOS",
                            null,
                            "EMPLEADO_NO_UPDATED"
                    );
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                }

                // Guardar el empleado actualizado
                Empleado_Model empleadoGuardado = empleadoServices.save(empleado);

                Response<Empleado_Model> response = new Response<>(
                        "200",
                        "Empleado actualizado satisfactoriamente",
                        empleadoGuardado,
                        "EMPLEADO_UPDATED"
                );
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                Response<Empleado_Model> response = new Response<>(
                        "404",
                        "Empleado no encontrado para la identificación proporcionada",
                        null,
                        "EMPLEADO_NOT_FOUND"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            logger.error("Error al actualizar al empleado: ", e);

            Response<Empleado_Model> response = new Response<>(
                    "500",
                    "Error al actualizar al empleado: " + e.getMessage(),
                    null,
                    "INTERNAL_SERVER_ERROR"
            );
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