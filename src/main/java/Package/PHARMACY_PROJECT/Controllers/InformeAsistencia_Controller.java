package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.InformeAsistencia_Model;
import Package.PHARMACY_PROJECT.Services.InformeAsistencia_Services;
import Package.PHARMACY_PROJECT.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/informe-asistencia")
public class InformeAsistencia_Controller {

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
}