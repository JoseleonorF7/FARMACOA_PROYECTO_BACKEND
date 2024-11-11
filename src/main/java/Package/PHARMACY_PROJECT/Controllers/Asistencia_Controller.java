package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.Reportes.EmpleadoRanking;
import Package.PHARMACY_PROJECT.Models.Reportes.EmpleadoReporte;
import Package.PHARMACY_PROJECT.Models.Reportes.PromedioMensual;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteConsolidado;
import Package.PHARMACY_PROJECT.Services.Asistencia_Services;
import Package.PHARMACY_PROJECT.Response;
import Package.PHARMACY_PROJECT.Services.Empleado_Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
            return "TEMPRANO";
        } else if (diferenciaMinutos > RANGO_TARDE) {
            return "TARDE";
        } else {
            return "PUNTUAL";
        }
    }

    // Método para calcular el estado de la entrada
    public String calcularEstadoSalida(LocalTime horaSalida) {
        // Calcular la diferencia en minutos entre la hora de entrada y la hora de referencia
        long diferenciaMinutos = ChronoUnit.MINUTES.between(HORA_REFERENCIA_SALIDA, horaSalida);

        // Determinar el estado en función de la diferencia de minutos
        if (diferenciaMinutos < RANGO_TEMPRANO) {
            return "TEMPRANO";
        } else if (diferenciaMinutos > RANGO_TARDE) {
            return "TARDE";
        } else {
            return "PUNTUAL";
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

    @GetMapping("/todas")
    public ResponseEntity<Response<List<Asistencia_Model>>> obtenerTodasLasAsistencias() {
        try {
            List<Asistencia_Model> asistencias = asistenciaServices.findAll();

            // Calcular la diferencia de tiempo para la entrada y salida
            for (Asistencia_Model asistencia : asistencias) {
                // Calcular la diferencia de tiempo para la entrada
                String diferenciaEntrada = asistencia.calcularDiferenciaTiempoEntrada();
                asistencia.setDiferenciaTiempoEntrada(diferenciaEntrada);

                // Calcular la diferencia de tiempo para la salida
                String diferenciaSalida = asistencia.calcularDiferenciaTiempoSalida();
                asistencia.setDiferenciaTiempoSalida(diferenciaSalida);
            }

            Response<List<Asistencia_Model>> response = new Response<>("200", "Asistencias obtenidas satisfactoriamente", asistencias, "ASISTENCIAS_OBTENIDAS");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            logger.error("Error al obtener todas las asistencias", e);
            Response<List<Asistencia_Model>> response = new Response<>("500", "Error al obtener las asistencias", null, "ERROR_DB");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/reporte/todas")
    public ResponseEntity<Response<ReporteConsolidado>> obtenerTodasLasAsistenciasReporte() {
        try {
            List<Asistencia_Model> asistencias = asistenciaServices.findAll();

            // Calcular la diferencia de tiempo para entrada y salida
            for (Asistencia_Model asistencia : asistencias) {
                String diferenciaEntrada = asistencia.calcularDiferenciaTiempoEntrada();
                asistencia.setDiferenciaTiempoEntrada(diferenciaEntrada);

                String diferenciaSalida = asistencia.calcularDiferenciaTiempoSalida();
                asistencia.setDiferenciaTiempoSalida(diferenciaSalida);
            }

            // Generar el reporte consolidado
            ReporteConsolidado reporteConsolidado = generarReporte(asistencias);

            // Crear la respuesta con el reporte consolidado
            Response<ReporteConsolidado> response = new Response<>();
            response.setCode("200");
            response.setMessage("Reporte generado exitosamente");
            response.setData(reporteConsolidado);
            response.setStatus("OK");

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            logger.error("Error al obtener todas las asistencias", e);

            Response<ReporteConsolidado> response = new Response<>(
                    "500",
                    "Error al obtener las asistencias",
                    null,
                    "ERROR_DB"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    public ReporteConsolidado generarReporte(List<Asistencia_Model> asistencias) {
        Map<String, EmpleadoReporte> reportePorEmpleado = new HashMap<>();
        Map<YearMonth, PromedioMensual> promediosMensuales = new HashMap<>();

        // Procesar cada registro de asistencia
        for (Asistencia_Model asistencia : asistencias) {
            String empleadoIdentificacion = asistencia.getEmpleado().getIdentificacion();

            if (empleadoIdentificacion == null || empleadoIdentificacion.isEmpty()) {
                logger.warn("Registro de asistencia con nombre de empleado nulo o vacío");
                continue;
            }

            YearMonth mesAnio = YearMonth.from(asistencia.getFecha());
            EmpleadoReporte datosEmpleado = reportePorEmpleado.computeIfAbsent(
                    empleadoIdentificacion,
                    k -> new EmpleadoReporte(empleadoIdentificacion)
            );

            if ("TARDE".equalsIgnoreCase(asistencia.getEstado()) && "ENTRADA".equalsIgnoreCase(asistencia.getTipoRegistro())) {
                long minutosTarde = EmpleadoRanking.extraerMinutos(asistencia.getDiferenciaTiempoEntrada());
                datosEmpleado.acumularTardanza(minutosTarde);
                promediosMensuales.computeIfAbsent(mesAnio, k -> new PromedioMensual()).acumularTardanzaEntrada(minutosTarde);
            } else if ("TEMPRANO".equalsIgnoreCase(asistencia.getEstado()) && "SALIDA".equalsIgnoreCase(asistencia.getTipoRegistro())) {
                long minutosTemprano = EmpleadoRanking.extraerMinutos(asistencia.getDiferenciaTiempoSalida());
                datosEmpleado.acumularSalidaTemprana(minutosTemprano);
                promediosMensuales.computeIfAbsent(mesAnio, k -> new PromedioMensual()).acumularSalidaTemprana(minutosTemprano);
            }
        }

        // Calcular rankings para tardanzas y salidas tempranas
        List<EmpleadoRanking> rankingTardanzas = generarRanking(reportePorEmpleado, true);
        List<EmpleadoRanking> rankingSalidasTempranas = generarRanking(reportePorEmpleado, false);

        // Calcular promedios mensuales
        calcularPromediosMensuales(promediosMensuales);

        // Crear reporte final consolidado
        ReporteConsolidado reporteFinal = new ReporteConsolidado(reportePorEmpleado, rankingTardanzas, rankingSalidasTempranas, promediosMensuales);
        logger.info("Reporte final generado con éxito");
        return reporteFinal;
    }
    // Genera el ranking en base a tardanzas (true) o salidas tempranas (false)
    private List<EmpleadoRanking> generarRanking(Map<String, EmpleadoReporte> reportePorEmpleado, boolean esTardanza) {
        return reportePorEmpleado.values().stream()
                .map(emp -> {
                    EmpleadoRanking ranking = new EmpleadoRanking(emp.getIdentificacionEmpleado());
                    if (esTardanza) {
                        ranking.acumularMinutosTardeEntrada(emp.getTotalMinutosTardeEntrada());
                        logger.debug("Empleado {} tiene acumulado {} minutos tarde", emp.getIdentificacionEmpleado(), emp.getTotalMinutosTardeEntrada());
                    } else {
                        ranking.acumularMinutosTempranoSalida(emp.getTotalMinutosTempranoSalida());
                        logger.debug("Empleado {} tiene acumulado {} minutos temprano", emp.getIdentificacionEmpleado(), emp.getTotalMinutosTempranoSalida());
                    }
                    return ranking;
                })
                .sorted(esTardanza ? EmpleadoRanking.comparadorTardeEntrada() : EmpleadoRanking.comparadorTempranoSalida())
                .collect(Collectors.toList());
    }

    // Calcula y muestra promedios mensuales
    private void calcularPromediosMensuales(Map<YearMonth, PromedioMensual> promediosMensuales) {
        promediosMensuales.forEach((mes, promedio) -> {
            double promedioTardanza = promedio.calcularPromedioTardanzaEntrada();
            double promedioSalidaTemprana = promedio.calcularPromedioSalidaTemprana();
            logger.info("Mes: {}, Promedio Tardanza (min): {}, Promedio Salida Temprana (min): {}", mes, promedioTardanza, promedioSalidaTemprana);
        });
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
    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<Response<List<Asistencia_Model>>> obtenerAsistenciasPorEmpleadoId(@PathVariable long empleadoId) {
        try {
            // Llamar al servicio para obtener las asistencias del empleado por su ID
            List<Asistencia_Model> asistencias = asistenciaServices.findByEmpleadoId(empleadoId);

            // Verificar si se encontraron asistencias
            if (asistencias.isEmpty()) {
                logger.warn("No se encontraron asistencias para el empleado con ID: " + empleadoId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("404", "No se encontraron asistencias para el empleado", null, "ASISTENCIAS_NO_ENCONTRADAS"));
            }
            // Calcular la diferencia de tiempo para la entrada y salida de cada asistencia
            for (Asistencia_Model asistencia : asistencias) {
                // Calcular la diferencia de tiempo para la entrada
                String diferenciaEntrada = asistencia.calcularDiferenciaTiempoEntrada();
                asistencia.setDiferenciaTiempoEntrada(diferenciaEntrada); // Establecer el valor de diferencia de entrada

                // Calcular la diferencia de tiempo para la salida
                String diferenciaSalida = asistencia.calcularDiferenciaTiempoSalida();
                asistencia.setDiferenciaTiempoSalida(diferenciaSalida); // Establecer el valor de diferencia de salida
            }


            // Si se encontraron asistencias, retornar la lista con una respuesta exitosa
            Response<List<Asistencia_Model>> response = new Response<>("200", "Asistencias obtenidas satisfactoriamente", asistencias, "ASISTENCIAS_OBTENIDAS");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            // Manejo de errores
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


