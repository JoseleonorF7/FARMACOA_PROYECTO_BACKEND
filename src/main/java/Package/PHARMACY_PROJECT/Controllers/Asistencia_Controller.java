package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.Reportes.ComparativaAsistencia_DTO;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteEmpleado_DTO;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteMensual_DTO;
import Package.PHARMACY_PROJECT.Services.Asistencia_Services;
import Package.PHARMACY_PROJECT.Response;
import Package.PHARMACY_PROJECT.Services.Empleado_Services;
import Package.PHARMACY_PROJECT.Services.Horario_Services;
import Package.PHARMACY_PROJECT.Services.InformeAsistencia_PDF_Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/asistencia")
public class Asistencia_Controller {


    private static final Logger logger = LoggerFactory.getLogger(Asistencia_Controller.class);


    public static final int RANGO_TEMPRANO = -10; // 10 minutos antes
    public static final int RANGO_TARDE = 10;     // 10 minutos después
    public static final LocalTime HORA_REFERENCIA_ENTRADA = LocalTime.of(7, 0); // 7 am para entrada
    public static final LocalTime HORA_REFERENCIA_SALIDA = LocalTime.of(19, 0); // 7 pm para salida

    @Autowired
    private Asistencia_Services asistenciaServices;

    @Autowired
    private Empleado_Services empleadoServices;

    @Autowired
    private Horario_Services horarioServices;

    @Autowired
    private InformeAsistencia_PDF_Services informeAsistenciaPDFServices;

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

        // Obtener la fecha y hora actuales
        LocalDate fechaActual = LocalDate.now();
        LocalTime horaEntradaActual = LocalTime.now();

        // Validar y registrar la entrada para el primer bloque
        Optional<Asistencia_Model> asistencia1Optional = asistenciaServices.findByEmpleadoAndFechaAndTipoRegistro(empleado, fechaActual, "ENTRADA_1");
        if (!asistencia1Optional.isPresent()) {
            // Calcular la diferencia de tiempo

            LocalTime horaInicio1 = empleado.getHorario().getHoraInicio1();
            String estadoEntrada1 = calcularEstadoEntrada(empleado, horaEntradaActual, 1);
            Asistencia_Model asistencia1 = new Asistencia_Model(empleado, fechaActual, horaEntradaActual, estadoEntrada1, "ENTRADA_1");

            // Calcular la diferencia de tiempo de entrada y establecerla
            String diferenciaEntrada1 = asistencia1.calcularDiferenciaTiempoEntrada(empleado.getHorario().getHoraInicio1(), null);
            asistencia1.setDiferenciaTiempoEntrada(diferenciaEntrada1);

            asistenciaServices.save(asistencia1);
            logger.info("Entrada registrada para el primer bloque de horario del empleado: " + empleado.getNombre());
        } else {
            logger.warn("La entrada para el primer bloque ya fue registrada.");
        }

        // Validar y registrar la entrada para el segundo bloque si aplica
        if (empleado.getHorario().getHoraInicio2() != null) {
            Optional<Asistencia_Model> asistencia2Optional = asistenciaServices.findByEmpleadoAndFechaAndTipoRegistro(empleado, fechaActual, "ENTRADA_2");

            if (!asistencia2Optional.isPresent()) {
                // Verificar que haya al menos una diferencia de tiempo mínima entre entradas
                Optional<Asistencia_Model> ultimaAsistencia = asistenciaServices.findUltimaAsistenciaRegistrada(empleado);
                if (ultimaAsistencia.isPresent()) {
                    LocalTime ultimaHoraEntrada = ultimaAsistencia.get().getHoraEntrada();
                    if (ultimaHoraEntrada != null && ChronoUnit.MINUTES.between(ultimaHoraEntrada, horaEntradaActual) < 10) {
                        logger.error("Las entradas no pueden registrarse con menos de 10 minutos de diferencia.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new Response<>("400", "Intento de doble registro en un intervalo corto", null, "REGISTRO_DUPLICADO"));
                    }
                }


                String estadoEntrada2 = calcularEstadoEntrada(empleado, horaEntradaActual, 2);
                Asistencia_Model asistencia2 = new Asistencia_Model(empleado, fechaActual, horaEntradaActual, estadoEntrada2, "ENTRADA_2");

                // Calcular la diferencia de tiempo de entrada y establecerla
                String diferenciaEntrada2 = asistencia2.calcularDiferenciaTiempoEntrada(empleado.getHorario().getHoraInicio2(), null);
                asistencia2.setDiferenciaTiempoEntrada(diferenciaEntrada2);

                asistenciaServices.save(asistencia2);

                logger.info("Entrada registrada para el segundo bloque de horario del empleado: " + empleado.getNombre());
            } else {
                logger.warn("La entrada para el segundo bloque ya fue registrada.");
            }
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response<>("200", "Entrada registrada", null, "ENTRADA_REGISTRADA"));
    }

    @GetMapping("/todas")
    public ResponseEntity<Response<List<Asistencia_Model>>> obtenerTodasLasAsistencias() {
        try {
            List<Asistencia_Model> asistencias = asistenciaServices.findAll();



            Response<List<Asistencia_Model>> response = new Response<>("200", "Asistencias obtenidas satisfactoriamente", asistencias, "ASISTENCIAS_OBTENIDAS");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            logger.error("Error al obtener todas las asistencias", e);
            Response<List<Asistencia_Model>> response = new Response<>("500", "Error al obtener las asistencias", null, "ERROR_DB");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    public String calcularEstadoEntrada(Empleado_Model empleado, LocalTime horaEntrada, int bloque) {
        LocalTime horaBloque = (bloque == 1) ? empleado.getHorario().getHoraInicio1() : empleado.getHorario().getHoraInicio2();
        if (horaBloque == null) return "INVALIDO";

        long diferenciaMinutos = ChronoUnit.MINUTES.between(horaBloque, horaEntrada);

        // Caso especial para el primer bloque a las 7:00 AM
        if (bloque == 1 && horaBloque.equals(LocalTime.of(7, 0))) {
            if (diferenciaMinutos <= 10) return "PUNTUAL";  // Hasta 7:10 AM inclusive
            return "TARDE";                                // Después de 7:10 AM
        }

        // Lógica general para otros horarios
        return (diferenciaMinutos <= 0) ? "PUNTUAL" : "TARDE";
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


    @GetMapping("/empleadosTodos")
    public ResponseEntity<byte[]> reporteGeneralTodos(@RequestParam Integer mes) {
        try {
            // Log para ver el mes que se pasa al servicio
            logger.info("Generando reporte para el mes: {}", mes);

            // Llamada al servicio para obtener el reporte consolidado

            // Llamada al servicio para generar el PDF

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(null);
        } catch (Exception e) {
            logger.error("Error al generar el reporte", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/reporteMensual")
    public ResponseEntity<ReporteMensual_DTO> obtenerReporteMensual(@RequestParam Integer mes, @RequestParam Integer ano) {
        try {
            // Llama al servicio para obtener el reporte mensual
            ReporteMensual_DTO reporte = asistenciaServices.obtenerReporteGeneralMensual(mes, ano);

            // Retorna el reporte como respuesta con código 200 (OK)
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            // Maneja el error y retorna un mensaje de error detallado
            logger.error("Error al obtener el reporte consolidado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);  // O también puedes retornar un objeto de error con un mensaje más descriptivo
        }
    }

    @GetMapping("/reporteEmpleadoMensual")
    public ResponseEntity<Response<ReporteEmpleado_DTO>> obtenerReporteEmpleadoMensual(
            @RequestParam Long empleadoId,
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        try {
            // Llama al servicio para obtener el reporte del empleado específico
            ReporteEmpleado_DTO reporteEmpleado = asistenciaServices.obtenerReporteEmpleadoMensual(empleadoId, mes, anio);

            // Retorna el reporte como respuesta con código 200 (OK) usando la clase Response
            Response<ReporteEmpleado_DTO> response = Response.success(reporteEmpleado);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Manejo de errores de validación
            Response<ReporteEmpleado_DTO> response = Response.error("Parámetros inválidos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (NoSuchElementException e) {
            // Manejo de error cuando no se encuentra un reporte
            Response<ReporteEmpleado_DTO> response = Response.notFound("No se encontró el reporte solicitado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            // Manejo de errores generales
            Response<ReporteEmpleado_DTO> response = Response.internalServerError("Error interno: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/reporteEmpleadoFecha")
    public ResponseEntity<ReporteEmpleado_DTO> obtenerReporteEmpleadoFecha(
            @RequestParam Long empleadoId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {
        try {
            // Llama al servicio para obtener el reporte del empleado en la fecha específica
            ReporteEmpleado_DTO reporteEmpleado = asistenciaServices.obtenerReporteEmpleadoFecha(empleadoId, fecha);

            // Retorna el reporte como respuesta con código 200 (OK)
            return ResponseEntity.ok(reporteEmpleado);
        } catch (Exception e) {
            // Maneja el error y retorna un mensaje de error detallado
            logger.error("Error al obtener el reporte del empleado en fecha específica", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);  // O puedes retornar un objeto de error con un mensaje más descriptivo
        }
    }

    @GetMapping("/reporteComparativo/grafica")
    public ResponseEntity<Map<String, Object>> obtenerDatosGrafica(
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        try {
            Map<String, Object> datosGrafica = asistenciaServices.obtenerComparativaAsistencia(mes, anio);
            return ResponseEntity.ok(datosGrafica);
        } catch (Exception e) {
            e.printStackTrace(); // Esto imprimirá el error completo en los logs del servidor
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al generar la gráfica", "details", e.getMessage()));
        }
    }





    @DeleteMapping("/eliminar/todos")
    public ResponseEntity<Response<Void>> deleteAllAsistencias() {
        try {
            asistenciaServices.deleteAll();

            Response<Void> response = new Response<>("200", "Todas las asistencias eliminadas satisfactoriamente", null, "ALL_ASISTENCIAS_DELETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al eliminar todas las asistencias: ", e);

            Response<Void> response = new Response<>("500", "Error al eliminar todas las asistencias", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Método para obtener las asistencias de un empleado por su ID
    // Método para obtener las asistencias de un empleado por su ID
    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<Response<List<Asistencia_Model>>> obtenerAsistenciasPorEmpleadoId(@PathVariable long empleadoId) {
        try {
            // Llamar al servicio para obtener las asistencias del empleado por su ID
            List<Asistencia_Model> asistencias = asistenciaServices.findByEmpleadoId(empleadoId);

            // Verificar si se encontraron asistencias
            if (asistencias == null || asistencias.isEmpty()) {
                logger.warn("No se encontraron asistencias para el empleado con ID: " + empleadoId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("404", "No se encontraron asistencias para el empleado", null, "ASISTENCIAS_NO_ENCONTRADAS"));
            }

            // Definir referencias de tiempo para la entrada y salida
            LocalTime horaReferenciaEntrada1 = LocalTime.of(8, 0); // Ejemplo de referencia para la entrada
            LocalTime horaReferenciaEntrada2 = LocalTime.of(9, 0); // Ejemplo de segunda referencia para la entrada

            // Calcular la diferencia de tiempo para la entrada y salida de cada asistencia
            for (Asistencia_Model asistencia : asistencias) {
                try {
                    // Calcular y establecer la diferencia de tiempo para la entrada
                    String diferenciaEntrada = asistencia.calcularDiferenciaTiempoEntrada(horaReferenciaEntrada1, horaReferenciaEntrada2);
                    asistencia.setDiferenciaTiempoEntrada(diferenciaEntrada);



                } catch (Exception ex) {
                    logger.error("Error al calcular la diferencia de tiempo para la asistencia ID: " + asistencia.getId(), ex);
                    asistencia.setDiferenciaTiempoEntrada("Error");
                }
            }

            // Retornar la lista de asistencias con una respuesta exitosa
            Response<List<Asistencia_Model>> response = new Response<>("200", "Asistencias obtenidas satisfactoriamente", asistencias, "ASISTENCIAS_OBTENIDAS");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            // Manejo de errores generales
            logger.error("Error al obtener las asistencias del empleado con ID: " + empleadoId, e);
            Response<List<Asistencia_Model>> response = new Response<>("500", "Error al obtener las asistencias", null, "ERROR_DB");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    // Método para obtener las asistencias de un empleado filtradas por mes
    @GetMapping("/empleado-mes/{empleadoId}/{mes}")
    public ResponseEntity<Response<List<Asistencia_Model>>> obtenerAsistenciasPorEmpleadoIdYMes(@PathVariable long empleadoId, @PathVariable int mes) {
        try {
            // Llamamos al servicio para obtener las asistencias filtradas por empleado y mes
            List<Asistencia_Model> asistenciasFiltradas = asistenciaServices.obtenerAsistenciasPorEmpleadoIdYMes(empleadoId, mes);

            // Verificar si se encontraron asistencias
            if (asistenciasFiltradas.isEmpty()) {
                logger.warn("No se encontraron asistencias para el empleado con ID: " + empleadoId + " en el mes: " + mes);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("404", "No se encontraron asistencias para el empleado en el mes especificado", null, "ASISTENCIAS_NO_ENCONTRADAS"));
            }

            // Si se encontraron asistencias filtradas, retornar la lista con una respuesta exitosa
            Response<List<Asistencia_Model>> response = new Response<>("200", "Asistencias filtradas por mes obtenidas satisfactoriamente", asistenciasFiltradas, "ASISTENCIAS_FILTRADAS");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            // Manejo de errores
            logger.error("Error al obtener las asistencias del empleado con ID: " + empleadoId + " para el mes: " + mes, e);
            Response<List<Asistencia_Model>> response = new Response<>("500", "Error al obtener las asistencias", null, "ERROR_DB");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}


