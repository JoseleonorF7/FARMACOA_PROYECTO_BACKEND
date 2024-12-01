package Package.PHARMACY_PROJECT.Models.Reportes;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReporteMensual_DTO {
    private int mes;
    private int año;
    private int totalAsistencias;
    private Map<String, Integer> asistenciasPorEstado; // Para contar puntuales y tardes
    private List<EstadisticasEmpleado_DTO> estadisticasPorEmpleado;

}
