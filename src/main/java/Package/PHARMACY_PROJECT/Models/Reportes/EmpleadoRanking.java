package Package.PHARMACY_PROJECT.Models.Reportes;

import java.util.Comparator;

public class EmpleadoRanking {
    private String identificacionEmpleado;
    private long totalMinutosTardeEntrada;
    private long totalMinutosTempranoSalida;

    public EmpleadoRanking(String identificacionEmpleado) {
        this.identificacionEmpleado = identificacionEmpleado;
    }

    public void acumularMinutosTardeEntrada(long minutos) {
        this.totalMinutosTardeEntrada += minutos;
    }

    public void acumularMinutosTempranoSalida(long minutos) {
        this.totalMinutosTempranoSalida += minutos;
    }

    public String getIdentificacionEmpleado() {
        return identificacionEmpleado;
    }

    public long getTotalMinutosTardeEntrada() {
        return totalMinutosTardeEntrada;
    }

    public long getTotalMinutosTempranoSalida() {
        return totalMinutosTempranoSalida;
    }

    // Comparador para ordenar empleados por total de minutos de tardanza en entrada
    public static Comparator<EmpleadoRanking> comparadorTardeEntrada() {
        return Comparator.comparingLong(EmpleadoRanking::getTotalMinutosTardeEntrada).reversed();
    }

    // Comparador para ordenar empleados por total de minutos de salida temprano
    public static Comparator<EmpleadoRanking> comparadorTempranoSalida() {
        return Comparator.comparingLong(EmpleadoRanking::getTotalMinutosTempranoSalida).reversed();
    }

    @Override
    public String toString() {
        return "Empleado: " + identificacionEmpleado +
                ", Total Tardanza Entrada (min): " + totalMinutosTardeEntrada +
                ", Total Salida Temprano (min): " + totalMinutosTempranoSalida;
    }


    // Método auxiliar para extraer minutos de una cadena de texto en formato "Tarde por X hora(s) y Y minuto(s)"
    public static long extraerMinutos(String diferenciaTiempo) {
        if (diferenciaTiempo == null || diferenciaTiempo.isEmpty() || diferenciaTiempo.contains("No disponible")) {
            return 0;
        }

        long horas = 0;
        long minutos = 0;

        String[] partes = diferenciaTiempo.split(" ");
        for (int i = 0; i < partes.length; i++) {
            if (partes[i].equals("hora(s)")) {
                horas = Long.parseLong(partes[i - 1]);
            } else if (partes[i].equals("minuto(s)")) {
                minutos = Long.parseLong(partes[i - 1]);
            }
        }

        return (horas * 60) + minutos;
    }
}
