package Package.PHARMACY_PROJECT.Models.Reportes;

import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadisticasEmpleado_DTO {
    private String empleadoNombre;
    private String totalTarde; // Total de tardanzas en formato "minutos y segundos"
    private int llegadasTarde; // Número de llegadas tarde
    private String totalPuntual; // Total de llegadas puntuales
    private int llegadasPuntuales; // Número de llegadas puntuales


}
