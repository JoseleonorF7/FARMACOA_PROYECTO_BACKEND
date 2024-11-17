package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.InformeAsistencia_Model;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteConsolidado;
import Package.PHARMACY_PROJECT.Services.Asistencia_Services;
import Package.PHARMACY_PROJECT.Services.Empleado_Services;
import Package.PHARMACY_PROJECT.Services.InformeAsistencia_Services;
import Package.PHARMACY_PROJECT.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/informe-asistencia")
public class InformeAsistencia_Controller {

    @Autowired
    private Asistencia_Services asistenciaServices;

    @Autowired
    private Empleado_Services empleadoServices;

    @Autowired
    private InformeAsistencia_Services informeAsistenciaServices;

    @PostMapping
    public ResponseEntity<Response<InformeAsistencia_Model>> saveInforme(@RequestBody InformeAsistencia_Model informeAsistencia) {
        try {
            InformeAsistencia_Model informeGenerado = informeAsistenciaServices.save(informeAsistencia);
            Response<InformeAsistencia_Model> response = new Response<>("200", "Informe generado satisfactoriamente", informeGenerado, "INFORME_GENERATED");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Response<InformeAsistencia_Model> response = new Response<>("500", "Error al generar el informe", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteInforme(@PathVariable Long id) {
        try {
            informeAsistenciaServices.deleteById(id);
            Response<Void> response = new Response<>("200", "Informe eliminado satisfactoriamente", null, "INFORME_DELETED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<Void> response = new Response<>("500", "Error al eliminar el informe", null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/pdf/empleado/{id}")
    public ResponseEntity<byte[]> getEmployeeAttendancePdfById(@PathVariable String id) {
        try {
            Optional<Empleado_Model> empleadoOptional = empleadoServices.findByIdentificacion(id);

            if (!empleadoOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Empleado_Model empleado = empleadoOptional.get();
            List<Asistencia_Model> asistencias = asistenciaServices.findByEmpleadoId(empleado.getId());

            byte[] pdfBytes = informeAsistenciaServices.generateEmployeeAttendancePdf(empleado, asistencias,null);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "informe_asistencia_empleado.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/pdf/empleado-mes/{id}/{mes}")
    public ResponseEntity<byte[]> getEmployeeAttendancePdfById(@PathVariable String id, @PathVariable(required = false) Integer mes) {
        try {
            Optional<Empleado_Model> empleadoOptional = empleadoServices.findByIdentificacion(id);

            if (!empleadoOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Empleado_Model empleado = empleadoOptional.get();
            List<Asistencia_Model> asistencias = asistenciaServices.obtenerAsistenciasPorEmpleadoIdYMes(empleado.getId(),mes);

            byte[] pdfBytes = informeAsistenciaServices.generateEmployeeAttendancePdf(empleado, asistencias, mes); // Se pasa el mes si está presente

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "informe_asistencia_empleado_" + (mes != null ? mes : "todos") + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/pdf/empleados")
    public ResponseEntity<byte[]> getAllEmployeeAttendancePdf() {
        try {
            // Obtener el reporte consolidado de asistencias desde el servicio
            ReporteConsolidado reporteConsolidado = asistenciaServices.obtenerReporteConsolidado();

            if (reporteConsolidado == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Generar el PDF usando los datos del reporte consolidado
            byte[] pdfBytes = informeAsistenciaServices.generateAllEmployeesAttendancePdf(reporteConsolidado);

            // Establecer las cabeceras para el archivo PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "informe_asistencia_todos_los_empleados.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}