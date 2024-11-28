package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.Horario_Model;
import Package.PHARMACY_PROJECT.Models.Reportes.EmpleadoRanking;
import Package.PHARMACY_PROJECT.Models.Reportes.EmpleadoReporte;
import Package.PHARMACY_PROJECT.Models.Reportes.PromedioMensual;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteConsolidado;
import Package.PHARMACY_PROJECT.Services.Asistencia_Services;
import Package.PHARMACY_PROJECT.Response;
import Package.PHARMACY_PROJECT.Services.Empleado_Services;
import Package.PHARMACY_PROJECT.Services.Horario_Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
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

    @Autowired
    private Horario_Services horarioServices;

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
            String estadoEntrada1 = calcularEstadoEntrada(empleado, horaEntradaActual, 1);
            Asistencia_Model asistencia1 = new Asistencia_Model(empleado, fechaActual, horaEntradaActual, null, estadoEntrada1, "ENTRADA_1");
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
                        logger.error("Las entradas no pueden registrarse con menos de 5 minutos de diferencia.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new Response<>("400", "Intento de doble registro en un intervalo corto", null, "REGISTRO_DUPLICADO"));
                    }
                }

                String estadoEntrada2 = calcularEstadoEntrada(empleado, horaEntradaActual, 2);
                Asistencia_Model asistencia2 = new Asistencia_Model(empleado, fechaActual, horaEntradaActual, null, estadoEntrada2, "ENTRADA_2");
                asistenciaServices.save(asistencia2);
                logger.info("Entrada registrada para el segundo bloque de horario del empleado: " + empleado.getNombre());
            } else {
                logger.warn("La entrada para el segundo bloque ya fue registrada.");
            }
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response<>("200", "Entrada registrada", null, "ENTRADA_REGISTRADA"));
    }


    @PostMapping("/salida/{huella}")
    public ResponseEntity<Response<Asistencia_Model>> registrarSalida(@PathVariable String huella) {
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
        LocalTime horaSalidaActual = LocalTime.now();

        // Verificar y registrar salida para el primer bloque
        Optional<Asistencia_Model> entrada1Optional = asistenciaServices.findByEmpleadoAndFechaAndTipoRegistro(empleado, fechaActual, "ENTRADA_1");
        if (entrada1Optional.isPresent()) {
            Optional<Asistencia_Model> salida1Optional = asistenciaServices.findByEmpleadoAndFechaAndTipoRegistro(empleado, fechaActual, "SALIDA_1");
            if (!salida1Optional.isPresent()) {
                String estadoSalida1 = calcularEstadoSalida(empleado, horaSalidaActual, 1);
                Asistencia_Model salida1 = new Asistencia_Model(empleado, fechaActual, null, horaSalidaActual, estadoSalida1, "SALIDA_1");
                asistenciaServices.save(salida1);
                logger.info("Salida registrada para el primer bloque de horario del empleado: " + empleado.getNombre());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new Response<>("200", "Salida registrada", salida1, "SALIDA_REGISTRADA"));
            }
        }

        // Verificar y registrar salida para el segundo bloque si aplica
        if (empleado.getHorario().getHoraInicio2() != null) {
            Optional<Asistencia_Model> entrada2Optional = asistenciaServices.findByEmpleadoAndFechaAndTipoRegistro(empleado, fechaActual, "ENTRADA_2");
            if (entrada2Optional.isPresent()) {
                Optional<Asistencia_Model> salida2Optional = asistenciaServices.findByEmpleadoAndFechaAndTipoRegistro(empleado, fechaActual, "SALIDA_2");
                if (!salida2Optional.isPresent()) {
                    String estadoSalida2 = calcularEstadoSalida(empleado, horaSalidaActual, 2);
                    Asistencia_Model salida2 = new Asistencia_Model(empleado, fechaActual, null, horaSalidaActual, estadoSalida2, "SALIDA_2");
                    asistenciaServices.save(salida2);
                    logger.info("Salida registrada para el segundo bloque de horario del empleado: " + empleado.getNombre());
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(new Response<>("200", "Salida registrada", salida2, "SALIDA_REGISTRADA"));
                }
            }
        }

        logger.error("No se puede registrar la salida: asegúrese de registrar primero la entrada correspondiente.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Response<>("400", "No se puede registrar la salida", null, "SALIDA_INVALIDA"));
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


    public String calcularEstadoSalida(Empleado_Model empleado, LocalTime horaSalida, int bloque) {
        LocalTime horaBloque = (bloque == 1) ? empleado.getHorario().getHoraFin1() : empleado.getHorario().getHoraFin2();
        if (horaBloque == null) return "INVALIDO";

        long diferenciaMinutos = ChronoUnit.MINUTES.between(horaBloque, horaSalida);

        if (diferenciaMinutos < RANGO_TEMPRANO) return "TEMPRANO";
        if (diferenciaMinutos > RANGO_TARDE) return "TARDE";
        return "PUNTUAL";
    }
    // Métodos existentes

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

            for (Asistencia_Model asistencia : asistencias) {
                // Obteniendo horarios según el turno del empleado
                LocalTime horaInicio1 = asistencia.getEmpleado().getHorario().getHoraInicio1();
                LocalTime horaFin1 = asistencia.getEmpleado().getHorario().getHoraFin1();
                LocalTime horaInicio2 = asistencia.getEmpleado().getHorario().getHoraInicio2();
                LocalTime horaFin2 = asistencia.getEmpleado().getHorario().getHoraFin2();

                // Calcular diferencias de entrada y salida
                String diferenciaEntrada = asistencia.calcularDiferenciaTiempoEntrada(horaInicio1, horaInicio2);
                asistencia.setDiferenciaTiempoEntrada(diferenciaEntrada);

                String diferenciaSalida = asistencia.calcularDiferenciaTiempoSalida(horaFin1, horaFin2);
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

    @GetMapping("/todas/reporte")
    public ResponseEntity<Response<ReporteConsolidado>> obtenerTodasLasAsistenciasReporte() {
        try {
            ReporteConsolidado reporteConsolidado = asistenciaServices.obtenerReporteConsolidado();

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
            LocalTime horaReferenciaSalida1 = LocalTime.of(17, 0); // Ejemplo de referencia para la salida
            LocalTime horaReferenciaSalida2 = LocalTime.of(18, 0); // Ejemplo de segunda referencia para la salida

            // Calcular la diferencia de tiempo para la entrada y salida de cada asistencia
            for (Asistencia_Model asistencia : asistencias) {
                try {
                    // Calcular y establecer la diferencia de tiempo para la entrada
                    String diferenciaEntrada = asistencia.calcularDiferenciaTiempoEntrada(horaReferenciaEntrada1, horaReferenciaEntrada2);
                    asistencia.setDiferenciaTiempoEntrada(diferenciaEntrada);

                    // Calcular y establecer la diferencia de tiempo para la salida
                    String diferenciaSalida = asistencia.calcularDiferenciaTiempoSalida(horaReferenciaSalida1, horaReferenciaSalida2);
                    asistencia.setDiferenciaTiempoSalida(diferenciaSalida);

                } catch (Exception ex) {
                    logger.error("Error al calcular la diferencia de tiempo para la asistencia ID: " + asistencia.getId(), ex);
                    asistencia.setDiferenciaTiempoEntrada("Error");
                    asistencia.setDiferenciaTiempoSalida("Error");
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


