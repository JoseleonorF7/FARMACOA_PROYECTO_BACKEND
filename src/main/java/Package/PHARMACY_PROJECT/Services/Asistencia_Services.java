package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.Reportes.EmpleadoRanking;
import Package.PHARMACY_PROJECT.Models.Reportes.PromedioMensual;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteConsolidado;
import Package.PHARMACY_PROJECT.Models.Reportes.EmpleadoReporte;

import Package.PHARMACY_PROJECT.Repository.Asistencia_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Asistencia_Services {

    private final Asistencia_Repository asistenciaRepository;

    @Autowired
    public Asistencia_Services(Asistencia_Repository asistenciaRepository) {
        this.asistenciaRepository = asistenciaRepository;
    }

    public List<Asistencia_Model> findAll() {
        return asistenciaRepository.findAll();
    }


    // Método para encontrar asistencia por empleado y fecha
    public Optional<Asistencia_Model> findByEmpleadoAndFecha(Empleado_Model empleado, LocalDate fecha) {
        return asistenciaRepository.findByEmpleadoAndFecha(empleado, fecha);
    }

    public Optional<Asistencia_Model> findByEmpleadoAndFechaAndTipoRegistro(Empleado_Model empleado, LocalDate fecha,String tipoRegistro) {
        return asistenciaRepository.findByEmpleadoAndFechaAndTipoRegistro(empleado, fecha,tipoRegistro);
    }

    // Método para guardar la asistencia
    public Asistencia_Model save(Asistencia_Model asistencia) {
        return asistenciaRepository.save(asistencia);
    }

    public void deleteById(Long id) {
        asistenciaRepository.deleteById(id);
    }

    public List<Asistencia_Model> findByEmpleadoId(Long empleadoId) {
        return asistenciaRepository.findByEmpleadoId(empleadoId);
    }

    // Método para obtener asistencias filtradas por empleado y mes
    public List<Asistencia_Model> obtenerAsistenciasPorEmpleadoIdYMes(long empleadoId, int mes) {
        List<Asistencia_Model> asistencias = asistenciaRepository.findByEmpleadoId(empleadoId);

        if (asistencias.isEmpty()) {
            return Collections.emptyList();  // Si no hay asistencias, retornamos una lista vacía
        }

        List<Asistencia_Model> asistenciasFiltradas = new ArrayList<>();

        for (Asistencia_Model asistencia : asistencias) {
            // Obtener el mes de la fecha de la asistencia (formato YYYY-MM-DD)
            String fecha = String.valueOf(asistencia.getFecha());  // "2024-11-10"
            int mesAsistencia = Integer.parseInt(fecha.substring(5, 7));  // Extraemos el mes (posición 5 a 7)

            if (mesAsistencia == mes) {
                // Si el mes coincide, añadimos la asistencia a la lista filtrada
                asistenciasFiltradas.add(asistencia);

                // Calcular y establecer las diferencias de tiempo
                String diferenciaEntrada = asistencia.calcularDiferenciaTiempoEntrada();
                asistencia.setDiferenciaTiempoEntrada(diferenciaEntrada);  // Establecer diferencia de entrada

                String diferenciaSalida = asistencia.calcularDiferenciaTiempoSalida();
                asistencia.setDiferenciaTiempoSalida(diferenciaSalida);  // Establecer diferencia de salida
            }
        }

        return asistenciasFiltradas;  // Retornar las asistencias filtradas
    }
    @Transactional
    public void deleteAll() {
        asistenciaRepository.deleteAll();
        asistenciaRepository.resetAutoIncrement(); // Si tu repositorio tiene un método para resetear el auto increment
    }

    public ReporteConsolidado obtenerReporteConsolidado() {
        List<Asistencia_Model> asistencias = findAll();
        // Calcular la diferencia de tiempo para entrada y salida
        for (Asistencia_Model asistencia : asistencias) {
            String diferenciaEntrada = asistencia.calcularDiferenciaTiempoEntrada();
            asistencia.setDiferenciaTiempoEntrada(diferenciaEntrada);

            String diferenciaSalida = asistencia.calcularDiferenciaTiempoSalida();
            asistencia.setDiferenciaTiempoSalida(diferenciaSalida);
        }

        return generarReporte(asistencias);
    }

    private ReporteConsolidado generarReporte(List<Asistencia_Model> asistencias) {
        Map<String, EmpleadoReporte> reportePorEmpleado = new HashMap<>();
        Map<YearMonth, PromedioMensual> promediosMensuales = new HashMap<>();

        for (Asistencia_Model asistencia : asistencias) {
            String empleadoIdentificacion = asistencia.getEmpleado().getIdentificacion();

            if (empleadoIdentificacion == null || empleadoIdentificacion.isEmpty()) {
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

        // Generar rankings y promedios
        List<EmpleadoRanking> rankingTardanzas = generarRanking(reportePorEmpleado, true);
        List<EmpleadoRanking> rankingSalidasTempranas = generarRanking(reportePorEmpleado, false);
        calcularPromediosMensuales(promediosMensuales);

        return new ReporteConsolidado(reportePorEmpleado, rankingTardanzas, rankingSalidasTempranas, promediosMensuales);
    }

    private List<EmpleadoRanking> generarRanking(Map<String, EmpleadoReporte> reportePorEmpleado, boolean esTardanza) {
        return reportePorEmpleado.values().stream()
                .map(emp -> {
                    EmpleadoRanking ranking = new EmpleadoRanking(emp.getIdentificacionEmpleado());
                    if (esTardanza) {
                        ranking.acumularMinutosTardeEntrada(emp.getTotalMinutosTardeEntrada());
                    } else {
                        ranking.acumularMinutosTempranoSalida(emp.getTotalMinutosTempranoSalida());
                    }
                    return ranking;
                })
                .sorted(esTardanza ? EmpleadoRanking.comparadorTardeEntrada() : EmpleadoRanking.comparadorTempranoSalida())
                .collect(Collectors.toList());
    }

    private void calcularPromediosMensuales(Map<YearMonth, PromedioMensual> promediosMensuales) {
        promediosMensuales.forEach((mes, promedio) -> {
            double promedioTardanza = promedio.calcularPromedioTardanzaEntrada();
            double promedioSalidaTemprana = promedio.calcularPromedioSalidaTemprana();
            // Puedes hacer logging aquí si lo necesitas
        });
    }

    public byte[] generarPdfReporteAsistencias(List<Asistencia_Model> asistencias) {
        // Lógica para generar el PDF
        // Puedes usar un servicio como iText o similar para crear el PDF con la lista de asistencias
        return new byte[0]; // Retorna el PDF en bytes
    }

}