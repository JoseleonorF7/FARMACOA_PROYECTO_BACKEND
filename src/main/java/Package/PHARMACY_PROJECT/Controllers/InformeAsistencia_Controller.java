package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.InformeAsistencia_Model;
import Package.PHARMACY_PROJECT.Models.Reportes.ComparativaAsistencia_DTO;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteEmpleado_DTO;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteMensual_DTO;
import Package.PHARMACY_PROJECT.PRUEBACORREOS.EmailService;
import Package.PHARMACY_PROJECT.Services.Asistencia_Services;
import Package.PHARMACY_PROJECT.Services.Empleado_Services;
import Package.PHARMACY_PROJECT.Services.InformeAsistencia_PDF_Services;
import Package.PHARMACY_PROJECT.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/informe-asistencia")
public class InformeAsistencia_Controller {

    private static final Logger log= LoggerFactory.getLogger(InformeAsistencia_Controller.class);

    @Autowired
    private Asistencia_Services asistenciaServices;

    @Autowired
    private Empleado_Services empleadoServices;

    @Autowired
    private InformeAsistencia_PDF_Services informeAsistenciaPDFServices;

    private final EmailService emailService;
    private LocalDate fechaEnvio = null;
    private boolean correoEnviado = false;

    public InformeAsistencia_Controller(EmailService emailService) {
        this.emailService = emailService;
    }
    @PostMapping("/programarEnvioCorreo")
    public ResponseEntity<Map<String, Object>> programarEnvioCorreo(@RequestBody Map<String, String> request) {
        String frecuencia = request.get("frecuencia");
        Map<String, Object> response = new HashMap<>();

        if (frecuencia == null || (!frecuencia.equals("quincenal") && !frecuencia.equals("mensual"))) {
            response.put("status", "error");
            response.put("message", "Frecuencia no válida.");
            return ResponseEntity.badRequest().body(response);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fechaEnvio = null;

        frecuencia = frecuencia.trim(); // Elimina espacios al inicio y al final


        // Lógica para determinar la fecha de envío basada en la frecuencia
        if ("quincenal".equals(frecuencia)) {
            LocalDateTime primerDiaMes = LocalDateTime.of(now.getYear(), now.getMonth(), 1, 0, 0);
            if (now.getDayOfMonth() <= 15) {
                fechaEnvio = primerDiaMes.plusDays(14); // Enviar el día 15
            } else {
                fechaEnvio = primerDiaMes.plusMonths(1).plusDays(14); // Día 15 del próximo mes
            }
        }else if ("mensual".equals(frecuencia)) {
            log.info("Calculando fecha para envío mensual...");
            try {
                // Obtener el último día del mes actual
                LocalDate lastDayOfMonth = now.toLocalDate().withDayOfMonth(now.toLocalDate().lengthOfMonth());

                // Convertirlo a LocalDateTime con hora 23:59:59 para evitar errores en cálculos
                fechaEnvio = lastDayOfMonth.atTime(23, 59, 59);
                log.info("Fecha calculada para envío mensual: {}", fechaEnvio);
            } catch (Exception e) {
                log.error("Error al calcular la fecha de envío mensual", e);
                throw new RuntimeException("No se pudo calcular la fecha de envío mensual.");
            }
        }

        // Validación de `fechaEnvio` y construcción de la respuesta
        if (fechaEnvio != null) {
            long secondsRemaining = ChronoUnit.SECONDS.between(now, fechaEnvio);

            if (secondsRemaining > 0) {
                Duration duration = Duration.ofSeconds(secondsRemaining);
                long days = duration.toDays();
                long hours = duration.toHoursPart();
                long minutes = duration.toMinutesPart();
                long seconds = duration.toSecondsPart();

                response.put("status", "success");
                response.put("message", "Correo programado exitosamente.");
                response.put("fechaEnvio", fechaEnvio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                response.put("tiempoRestante", String.format("%d días, %d horas, %d minutos, %d segundos", days, hours, minutes, seconds));
            } else {
                enviarCorreo();
                response.put("status", "success");
                response.put("message", "El correo ha sido enviado porque la fecha ya ha pasado.");
            }
        } else {
            response.put("status", "error");
            response.put("message", "Error al calcular la fecha de envío.");
        }

        return ResponseEntity.ok(response);
    }
    private void enviarCorreo() {
        if (correoEnviado) {
            return; // Evitar enviar el correo nuevamente
        }

        if (fechaEnvio == null) {
            throw new IllegalStateException("No se ha programado una fecha de envío.");
        }

        try {
            // Extraer mes y año de la fecha de envío
            int mes = fechaEnvio.getMonthValue();
            int ano = fechaEnvio.getYear();

            // Obtener el reporte mensual en formato PDF
            ReporteMensual_DTO reporteMensual = asistenciaServices.obtenerReporteGeneralMensual(mes, ano);

            if (reporteMensual == null) {
                throw new IllegalStateException("No hay datos disponibles para generar el reporte de asistencia.");
            }

            // Generar el archivo PDF
            byte[] pdfBytes = informeAsistenciaPDFServices.generateReporteMensualPdf(reporteMensual);

            // Configurar el asunto y cuerpo del correo
            String subject = "Reporte de Asistencia - " + fechaEnvio.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            String body = "Adjunto encontrarás el reporte de asistencia generado para el mes de " +
                    fechaEnvio.format(DateTimeFormatter.ofPattern("MMMM yyyy")) + ".";

            // Enviar el correo con el PDF adjunto
            emailService.sendSimpleMessage(
                    "farmacenterlasuperdrogueria@gmail.com",
                    subject,
                    body
            );

            correoEnviado = true; // Marcar como enviado
        } catch (Exception e) {
            // Manejo de errores
            throw new IllegalStateException("Error al enviar el correo con el reporte: " + e.getMessage(), e);
        }
    }

    // Método para ver el tiempo restante
    @GetMapping("/verTiempoRestante")
    public String verTiempoRestante() {
        if (fechaEnvio == null) {
            return "No se ha programado un envío.";
        }

        LocalDateTime now = LocalDateTime.now();
        long secondsRemaining = ChronoUnit.SECONDS.between(now, fechaEnvio);

        if (secondsRemaining > 0) {
            Duration duration = Duration.ofSeconds(secondsRemaining);
            long days = duration.toDays();
            long hours = duration.toHoursPart();
            long minutes = duration.toMinutesPart();
            long seconds = duration.toSecondsPart();

            return "Tiempo restante hasta el envío: " + days + " días, " + hours + " horas, "
                    + minutes + " minutos, " + seconds + " segundos.";
        } else if (!correoEnviado) {
            enviarCorreo();
            return "El correo ya ha sido enviado.";
        } else {
            return "El correo ya fue enviado.";
        }
    }

    @GetMapping("/reporteMensual/pdf/{mes}/{ano}")
    public ResponseEntity<?> getReporteMensualPdf(@PathVariable Integer mes, @PathVariable Integer ano) {
        try {
            // Validaciones iniciales (por ejemplo, valores de mes y año válidos)
            if (mes < 1 || mes > 12) {
                return ResponseEntity.badRequest().body(Response.error("El mes debe estar entre 1 y 12."));
            }
            if (ano < 2000 || ano > LocalDate.now().getYear()) {
                return ResponseEntity.badRequest().body(Response.error("El año no es válido."));
            }

            // Obtener el reporte mensual desde el servicio
            ReporteMensual_DTO reporteMensual = asistenciaServices.obtenerReporteGeneralMensual(mes, ano);

            // Verificar que el reporte no sea nulo o vacío
            if (reporteMensual == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.notFound("No hay datos para generar el reporte."));
            }

            // Generar el PDF usando el servicio de PDF
            byte[] pdfBytes = informeAsistenciaPDFServices.generateReporteMensualPdf(reporteMensual);

            // Configurar encabezados para el archivo PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "reporte_asistencia_" + mes + "_" + ano + ".pdf");

            // Retornar el PDF en caso de éxito
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            // Manejar errores inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.internalServerError("Ocurrió un error al generar el reporte: " + e.getMessage()));
        }
    }

    @GetMapping("/reporteEmpleado/pdf/{empleadoId}/{mes}/{ano}")
    public ResponseEntity<?> getReporteEmpleadoPdf(@PathVariable Integer empleadoId, @PathVariable Integer mes, @PathVariable Integer ano) {
        try {
            // Validaciones iniciales
            if (empleadoId <= 0) {
                return ResponseEntity.badRequest().body(Response.error("El ID del empleado debe ser mayor a 0."));
            }
            if (mes < 1 || mes > 12) {
                return ResponseEntity.badRequest().body(Response.error("El mes debe estar entre 1 y 12."));
            }
            if (ano < 2000 || ano > LocalDate.now().getYear()) {
                return ResponseEntity.badRequest().body(Response.error("El año no es válido."));
            }

            // Obtener los datos de asistencia para el empleado
            ReporteEmpleado_DTO reporteEmpleado = asistenciaServices.obtenerReporteEmpleadoMensual(Long.valueOf(empleadoId), mes, ano);

            // Validar que existan datos para el reporte
            if (reporteEmpleado == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.notFound("No hay datos para el reporte del empleado."));
            }

            // Generar el PDF
            byte[] pdfBytes = informeAsistenciaPDFServices.generateReporteEmpleadoPdf(reporteEmpleado);

            // Configurar encabezados
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "reporte_asistencia_empleado_" + empleadoId + "_" + mes + "_" + ano + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.internalServerError("Ocurrió un error al generar el reporte: " + e.getMessage()));
        }
    }

    @GetMapping("/reporteEmpleadoUnico/pdf/{empleadoId}/{fecha}")
    public ResponseEntity<?> getReporteEmpleadoUnicoPdf(
            @PathVariable Long empleadoId,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {
        try {
            // Validaciones iniciales
            if (empleadoId <= 0) {
                return ResponseEntity.badRequest().body(Response.error("El ID del empleado debe ser mayor a 0."));
            }
            if (fecha == null || fecha.isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest().body(Response.error("La fecha es inválida o está en el futuro."));
            }

            // Obtener datos de asistencia
            ReporteEmpleado_DTO reporteEmpleado = asistenciaServices.obtenerReporteEmpleadoFecha(empleadoId, fecha);

            // Validar que existan datos para el reporte
            if (reporteEmpleado == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Response.notFound("No hay datos para el reporte del empleado en la fecha especificada."));
            }

            // Generar el PDF
            byte[] pdfBytes = informeAsistenciaPDFServices.generateReporteEmpleadoFechaPdf(reporteEmpleado);

            // Validar que el PDF no está vacío
            if (pdfBytes == null || pdfBytes.length == 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Response.internalServerError("El archivo PDF está vacío."));
            }

            // Configurar encabezados
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("reporte_asistencia_empleado_fecha_" + empleadoId + "_" + fecha + ".pdf")
                    .build());

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.internalServerError("Ocurrió un error al generar el reporte: " + e.getMessage()));
        }
    }

    @GetMapping("/pdf/comparativo")
    public ResponseEntity<?> getComparativoAsistenciaPdf(
            @RequestParam Integer mes,
            @RequestParam Integer anio) {
        try {
            // Validaciones iniciales (validar mes y año)
            if (mes < 1 || mes > 12) {
                return ResponseEntity.badRequest().body(Response.error("El mes debe estar entre 1 y 12."));
            }
            if (anio < 2000 || anio > LocalDate.now().getYear()) {
                return ResponseEntity.badRequest().body(Response.error("El año no es válido."));
            }

            // Obtener los datos comparativos desde el servicio
            Map<String, Object> datosComparativa = asistenciaServices.obtenerComparativaAsistencia(mes, anio);

            // Verificar si hay datos disponibles para el reporte
            if (datosComparativa == null || datosComparativa.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound("No hay datos para generar el reporte comparativo."));
            }

            // Extraer los datos necesarios para el reporte
            int cantidadTardanzas = (Integer) datosComparativa.getOrDefault("cantidadTardanzas", 0);
            int cantidadPuntualidades = (Integer) datosComparativa.getOrDefault("cantidadPuntualidades", 0);
            List<ComparativaAsistencia_DTO> empleados =
                    (List<ComparativaAsistencia_DTO>) datosComparativa.getOrDefault("empleados", List.of());

            // Generar el PDF usando el servicio de PDF
            byte[] pdfBytes = informeAsistenciaPDFServices.generarReporteComparativoPdf(
                    cantidadTardanzas, cantidadPuntualidades, empleados, mes, anio);

            // Configurar encabezados para el archivo PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("reporte_comparativo_asistencia_" + mes + "_" + anio+ ".pdf")
                    .build());


            // Retornar el PDF en caso de éxito
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            // Manejo de errores inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.internalServerError("Ocurrió un error al generar el reporte: " + e.getMessage()));
        }
    }





}