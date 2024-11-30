package Package.PHARMACY_PROJECT.Models.Reportes;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ReporteEmpleado_DTO {
    private Long empleadoId;
    private String empleadoNombre;
    private int mes;
    private int año;
    private int totalAsistencias;
    private String totalTarde;
    private int llegadasTarde;
    private int llegadasPuntuales;
    private List<Asistencia_Model> asistencias; // Lista de detalles de las asistencias
    private LocalDate fecha;

}





