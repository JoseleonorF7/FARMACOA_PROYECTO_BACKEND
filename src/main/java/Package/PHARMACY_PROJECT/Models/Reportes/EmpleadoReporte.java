package Package.PHARMACY_PROJECT.Models.Reportes;

import lombok.Getter;
import lombok.Setter;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class EmpleadoReporte {
    private String identificacionEmpleado;
    private long totalMinutosTardeEntrada = 0;
    private long totalMinutosTempranoSalida = 0;

    public EmpleadoReporte(String identificacionEmpleado) {
        this.identificacionEmpleado = identificacionEmpleado;
    }

    public void acumularTardanza(long minutosTarde) {
        this.totalMinutosTardeEntrada += minutosTarde;
    }

    public void acumularSalidaTemprana(long minutosTemprano) {
        this.totalMinutosTempranoSalida += minutosTemprano;
    }

    public long getTotalMinutosTardeEntrada() {
        return totalMinutosTardeEntrada;
    }

    public long getTotalMinutosTempranoSalida() {
        return totalMinutosTempranoSalida;
    }

}
