package Package.PHARMACY_PROJECT.Models.Reportes;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PromedioMensual {
    private List<Long> minutosTardeEntrada = new ArrayList<>();
    private List<Long> minutosTempranoSalida = new ArrayList<>();

    // Constructor por defecto
    public PromedioMensual() {}
    public void acumularTardanzaEntrada(long minutos) {
        minutosTardeEntrada.add(minutos);
    }

    public void acumularSalidaTemprana(long minutos) {
        minutosTempranoSalida.add(minutos);
    }

    public double calcularPromedioTardanzaEntrada() {
        return minutosTardeEntrada.isEmpty() ? 0 : minutosTardeEntrada.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    public double calcularPromedioSalidaTemprana() {
        return minutosTempranoSalida.isEmpty() ? 0 : minutosTempranoSalida.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    @Override
    public String toString() {
        return "PromedioMensual{" +
                "promedioTardanzaEntrada=" + calcularPromedioTardanzaEntrada() +
                ", promedioSalidaTemprana=" + calcularPromedioSalidaTemprana() +
                '}';
    }
}
