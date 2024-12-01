package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.Reportes.ComparativaAsistencia_DTO;
import Package.PHARMACY_PROJECT.Models.Reportes.EstadisticasEmpleado_DTO;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteEmpleado_DTO;
import Package.PHARMACY_PROJECT.Models.Reportes.ReporteMensual_DTO;

import Package.PHARMACY_PROJECT.Repository.Asistencia_Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class Asistencia_Services {

    private static final Logger logger = LoggerFactory.getLogger(Asistencia_Services.class);

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
            // Obtener el mes de la fecha de la asistencia
            String fecha = String.valueOf(asistencia.getFecha());
            int mesAsistencia = Integer.parseInt(fecha.substring(5, 7));

            if (mesAsistencia == mes) {
                // Si el mes coincide, añadimos la asistencia a la lista filtrada
                asistenciasFiltradas.add(asistencia);

                // Obtener horarios del turno del empleado
                LocalTime horaInicio1 = asistencia.getEmpleado().getHorario().getHoraInicio1();
                LocalTime horaFin1 = asistencia.getEmpleado().getHorario().getHoraFin1();
                LocalTime horaInicio2 = asistencia.getEmpleado().getHorario().getHoraInicio2();
                LocalTime horaFin2 = asistencia.getEmpleado().getHorario().getHoraFin2();

                // Calcular y establecer las diferencias de tiempo
                String diferenciaEntrada = asistencia.calcularDiferenciaTiempoEntrada(horaInicio1, horaInicio2);
                asistencia.setDiferenciaTiempoEntrada(diferenciaEntrada);


            }
        }

        return asistenciasFiltradas;  // Retornar las asistencias filtradas
    }

    public Optional<Asistencia_Model> findUltimaAsistenciaRegistrada(Empleado_Model empleado) {
        return asistenciaRepository.findTopByEmpleadoOrderByHoraEntradaDesc(empleado);
    }


    @Transactional
    public void deleteAll() {
        asistenciaRepository.deleteAll();
        asistenciaRepository.resetAutoIncrement(); // Si tu repositorio tiene un método para resetear el auto increment
    }



    public List<Asistencia_Model> getAsistenciasByEmpleado(long idEmpleado, int mes) {
        // Obtener todas las asistencias del empleado
        List<Asistencia_Model> asistencias = asistenciaRepository.findByEmpleadoId(idEmpleado);

        // Filtrar las asistencias por mes
        List<Asistencia_Model> asistenciasFiltradas = asistencias.stream()
                .filter(asistencia -> {
                    int mesAsistencia = Integer.parseInt(asistencia.getFecha().toString().substring(5, 7));
                    return mesAsistencia == mes; // Filtrar por mes
                })
                .collect(Collectors.toList());

        return asistenciasFiltradas;
    }

    public List<Asistencia_Model> getAsistenciasByEmpleadoFecha(Long idEmpleado, String fecha) {
        // Obtener todas las asistencias del empleado
        List<Asistencia_Model> asistencias = asistenciaRepository.findByEmpleadoId(idEmpleado);

        // Filtrar las asistencias por la fecha proporcionada
        List<Asistencia_Model> asistenciasFiltradas = asistencias.stream()
                .filter(asistencia -> asistencia.getFecha().toString().equals(fecha)) // Filtrar por la fecha exacta
                .collect(Collectors.toList());

        return asistenciasFiltradas;
    }

    public ReporteMensual_DTO obtenerReporteComparativoAsistencia(Integer mes) {
        // Obtener las asistencias del mes especificado
        List<Asistencia_Model> asistencias = asistenciaRepository.findByMes(mes); // Este método debe ser implementado en el repositorio

        // Generar el reporte comparativo utilizando las asistencias obtenidas
        return null;
    }

//-------------------------------------------------------------------------------

    public ReporteMensual_DTO obtenerReporteGeneralMensual(int mes, int anio) {
        ReporteMensual_DTO reporte = new ReporteMensual_DTO();

        // Validación de parámetros
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
        }
        if (anio < 2000 || anio > LocalDate.now().getYear()) { // Ajusta el rango de años si es necesario
            throw new IllegalArgumentException("El año debe ser válido y no mayor al actual.");
        }

        // Obtener asistencias del mes
        List<Asistencia_Model> asistenciasDelMes = asistenciaRepository.findByMes(mes);
        if (asistenciasDelMes == null || asistenciasDelMes.isEmpty()) {
            throw new NoSuchElementException("No se encontraron asistencias para el mes y año proporcionados.");
        }

        // Filtrar asistencias por año
        asistenciasDelMes = asistenciasDelMes.stream()
                .filter(asistencia -> asistencia.getFecha().getYear() == anio)
                .collect(Collectors.toList());
        if (asistenciasDelMes.isEmpty()) {
            throw new NoSuchElementException("No se encontraron asistencias en el año especificado.");
        }

        // Agrupación y lógica del cálculo
        try {
            Map<Empleado_Model, List<Asistencia_Model>> asistenciasAgrupadas = asistenciasDelMes.stream()
                    .collect(Collectors.groupingBy(Asistencia_Model::getEmpleado));

            // Inicialización de variables para el reporte
            List<EstadisticasEmpleado_DTO> estadisticasPorEmpleado = new ArrayList<>();
            EstadisticasEmpleado_DTO empleadoConMayorTardanza = null;
            int maxTardanza = 0, asistenciasPuntuales = 0, asistenciasTardes = 0;

            for (Map.Entry<Empleado_Model, List<Asistencia_Model>> entrada : asistenciasAgrupadas.entrySet()) {
                Empleado_Model empleado = entrada.getKey();
                List<Asistencia_Model> asistenciasEmpleado = entrada.getValue();

                int totalMinutosTarde = 0, totalSegundosTarde = 0, llegadasTarde = 0, llegadasPuntuales = 0;

                for (Asistencia_Model asistencia : asistenciasEmpleado) {
                    String diferenciaTiempo = asistencia.getDiferenciaTiempoEntrada();
                    if (diferenciaTiempo != null) {
                        if (diferenciaTiempo.contains("Tarde")) {
                            llegadasTarde++;
                            asistenciasTardes++;
                            String tiempo = calcularTiempo(diferenciaTiempo);

                            // Validar el formato del tiempo
                            Pattern pattern = Pattern.compile("(\\d+) minutos y (\\d+) segundos");
                            Matcher matcher = pattern.matcher(tiempo);
                            if (matcher.find()) {
                                totalMinutosTarde += Integer.parseInt(matcher.group(1));
                                totalSegundosTarde += Integer.parseInt(matcher.group(2));
                            }
                        } else {
                            llegadasPuntuales++;
                            asistenciasPuntuales++;
                        }
                    }
                }

                // Ajustar segundos
                totalMinutosTarde += totalSegundosTarde / 60;
                totalSegundosTarde = totalSegundosTarde % 60;

                EstadisticasEmpleado_DTO estadisticasEmpleado = new EstadisticasEmpleado_DTO();
                estadisticasEmpleado.setEmpleadoNombre(empleado.getNombre());
                estadisticasEmpleado.setTotalTarde(String.format("%d minutos y %d segundos", totalMinutosTarde, totalSegundosTarde));
                estadisticasEmpleado.setLlegadasTarde(llegadasTarde);
                estadisticasEmpleado.setLlegadasPuntuales(llegadasPuntuales);
                estadisticasEmpleado.setTotalPuntual(String.format("%d asistencias puntuales", llegadasPuntuales));

                estadisticasPorEmpleado.add(estadisticasEmpleado);

                int tardanzaTotal = totalMinutosTarde * 60 + totalSegundosTarde;
                if (tardanzaTotal > maxTardanza) {
                    maxTardanza = tardanzaTotal;
                    empleadoConMayorTardanza = estadisticasEmpleado;
                }
            }

            // Completar reporte
            reporte.setMes(mes);
            reporte.setAño(anio);
            reporte.setTotalAsistencias(asistenciasPuntuales + asistenciasTardes);
            Map<String, Integer> asistenciasPorEstado = new HashMap<>();
            asistenciasPorEstado.put("puntuales", asistenciasPuntuales);
            asistenciasPorEstado.put("tardes", asistenciasTardes);
            reporte.setAsistenciasPorEstado(asistenciasPorEstado);
            reporte.setEstadisticasPorEmpleado(estadisticasPorEmpleado);

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte mensual: " + e.getMessage(), e);
        }

        return reporte;
    }


    private String calcularTiempo(String diferenciaTiempo) {
        int horas = 0, minutos = 0, segundos = 0;

        // Patrón para extraer horas, minutos y segundos
        Pattern pattern = Pattern.compile("(\\d+) hora\\(s\\) y (\\d+) minuto\\(s\\) y (\\d+) segundo\\(s\\)");
        Matcher matcher = pattern.matcher(diferenciaTiempo);

        if (matcher.find()) {
            horas = Integer.parseInt(matcher.group(1));
            minutos = Integer.parseInt(matcher.group(2));
            segundos = Integer.parseInt(matcher.group(3));
        } else {
            // Si solo hay horas y minutos (sin segundos)
            pattern = Pattern.compile("(\\d+) hora\\(s\\) y (\\d+) minuto\\(s\\)");
            matcher = pattern.matcher(diferenciaTiempo);
            if (matcher.find()) {
                horas = Integer.parseInt(matcher.group(1));
                minutos = Integer.parseInt(matcher.group(2));
            }
        }

        // Convertir horas a minutos y sumar minutos y segundos
        minutos += horas * 60;

        // Devolver el tiempo en formato "X minutos y Y segundos"
        return String.format("%d minutos y %d segundos", minutos, segundos);
    }

    // Método para reporte mensual de un empleado
    public ReporteEmpleado_DTO obtenerReporteEmpleadoMensual(Long empleadoId, int mes, int anio) {
        try {
            // Validaciones de entrada
            if (mes < 1 || mes > 12) {
                throw new IllegalArgumentException("El mes debe estar entre 1 y 12.");
            }
            if (anio < 2000 || anio > LocalDate.now().getYear()) {
                throw new IllegalArgumentException("El año debe ser válido y no mayor al actual.");
            }

            // Filtrar asistencias por mes
            List<Asistencia_Model> asistenciasDelMes = asistenciaRepository.findByMes(mes);
            if (asistenciasDelMes == null || asistenciasDelMes.isEmpty()) {
                throw new NoSuchElementException("No se encontraron asistencias para el mes proporcionado.");
            }

            // Filtrar por empleado y año
            List<Asistencia_Model> asistenciasEmpleado = asistenciasDelMes.stream()
                    .filter(asistencia -> asistencia.getEmpleado().getId().equals(empleadoId)
                            && asistencia.getFecha().getYear() == anio)
                    .collect(Collectors.toList());
            if (asistenciasEmpleado.isEmpty()) {
                throw new NoSuchElementException("No se encontraron asistencias para el empleado especificado en el año proporcionado.");
            }

            // Lógica de cálculo
            int totalMinutosTarde = 0, totalSegundosTarde = 0, llegadasTarde = 0, llegadasPuntuales = 0;
            List<Asistencia_Model> detallesAsistencias = new ArrayList<>();

            for (Asistencia_Model asistencia : asistenciasEmpleado) {
                detallesAsistencias.add(asistencia);

                String diferenciaTiempo = asistencia.getDiferenciaTiempoEntrada();
                if (diferenciaTiempo != null && diferenciaTiempo.contains("Tarde")) {
                    llegadasTarde++;
                    String tiempo = calcularTiempo(diferenciaTiempo);
                    Pattern pattern = Pattern.compile("(\\d+) minutos y (\\d+) segundos");
                    Matcher matcher = pattern.matcher(tiempo);
                    if (matcher.find()) {
                        totalMinutosTarde += Integer.parseInt(matcher.group(1));
                        totalSegundosTarde += Integer.parseInt(matcher.group(2));
                    }
                } else {
                    llegadasPuntuales++;
                }
            }

            totalMinutosTarde += totalSegundosTarde / 60;
            totalSegundosTarde = totalSegundosTarde % 60;

            // Crear el reporte
            ReporteEmpleado_DTO reporteEmpleado = new ReporteEmpleado_DTO();
            reporteEmpleado.setEmpleadoId(empleadoId);
            reporteEmpleado.setEmpleadoNombre(asistenciasEmpleado.get(0).getEmpleado().getNombre());
            reporteEmpleado.setMes(mes);
            reporteEmpleado.setAño(anio);
            reporteEmpleado.setTotalAsistencias(llegadasTarde + llegadasPuntuales);
            reporteEmpleado.setTotalTarde(String.format("%d minutos y %d segundos", totalMinutosTarde, totalSegundosTarde));
            reporteEmpleado.setLlegadasTarde(llegadasTarde);
            reporteEmpleado.setLlegadasPuntuales(llegadasPuntuales);
            reporteEmpleado.setAsistencias(detallesAsistencias);

            return reporteEmpleado;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte mensual del empleado: " + e.getMessage(), e);
        }
    }

    // Método para reporte por fecha de un empleado
    public ReporteEmpleado_DTO obtenerReporteEmpleadoFecha(Long empleadoId, LocalDate fecha) {
        try {
            // Validaciones
            if (empleadoId == null || fecha == null) {
                throw new IllegalArgumentException("Empleado ID y fecha no pueden ser nulos.");
            }

            // Buscar asistencias para la fecha y empleado
            List<Asistencia_Model> asistencias = asistenciaRepository.findByEmpleadoIdAndFecha(empleadoId,fecha);
            List<Asistencia_Model> asistenciasEmpleado = asistencias.stream()
                    .filter(asistencia -> asistencia.getEmpleado().getId().equals(empleadoId))
                    .collect(Collectors.toList());
            if (asistenciasEmpleado.isEmpty()) {
                throw new NoSuchElementException("No se encontraron asistencias para el empleado en la fecha especificada.");
            }

            // Crear reporte
            ReporteEmpleado_DTO reporteEmpleado = new ReporteEmpleado_DTO();
            reporteEmpleado.setFecha(fecha);
            reporteEmpleado.setEmpleadoId(empleadoId);
            reporteEmpleado.setEmpleadoNombre(asistenciasEmpleado.get(0).getEmpleado().getNombre());
            reporteEmpleado.setMes(fecha.getMonthValue());
            reporteEmpleado.setAño(fecha.getYear());
            reporteEmpleado.setAsistencias(asistenciasEmpleado);

            return reporteEmpleado;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte por fecha del empleado: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> obtenerComparativaAsistencia(Integer mes, Integer anio) {
        Map<String, Object> resultado = new HashMap<>();

        try {
            // Validar entradas
            if (mes == null || mes < 1 || mes > 12) {
                throw new IllegalArgumentException("El mes proporcionado es inválido. Debe estar entre 1 y 12.");
            }
            if (anio == null || anio < 1900) {
                throw new IllegalArgumentException("El año proporcionado es inválido. Debe ser mayor o igual a 1900.");
            }

            // Obtener asistencias del mes (sin filtrar por año aún)
            List<Asistencia_Model> asistenciasDelMes = asistenciaRepository.findByMes(mes);

            if (asistenciasDelMes == null) {
                throw new RuntimeException("No se encontraron registros de asistencia para el mes proporcionado.");
            }

            // Filtrar por año y agrupar por empleado
            Map<Long, ComparativaAsistencia_DTO> resumenPorEmpleado = new HashMap<>();
            int totalTardanzas = 0;
            int totalPuntualidades = 0;

            for (Asistencia_Model asistencia : asistenciasDelMes) {
                if (asistencia.getFecha() == null) {
                    throw new NullPointerException("Una de las asistencias no tiene fecha registrada.");
                }
                if (asistencia.getFecha().getYear() == anio) {
                    if (asistencia.getEmpleado() == null || asistencia.getEmpleado().getId() == null) {
                        throw new NullPointerException("Una de las asistencias tiene un empleado o ID nulo.");
                    }

                    Long empleadoId = asistencia.getEmpleado().getId();

                    // Si no existe en el mapa, inicializar
                    resumenPorEmpleado.putIfAbsent(empleadoId, new ComparativaAsistencia_DTO(
                            empleadoId,
                            asistencia.getEmpleado().getNombre(),
                            0, // Puntualidades
                            0  // Tardanzas
                    ));

                    // Clasificar si fue puntual o tarde
                    if (asistencia.getDiferenciaTiempoEntrada() != null
                            && asistencia.getDiferenciaTiempoEntrada().contains("Tarde")) {
                        resumenPorEmpleado.get(empleadoId).incrementarTardanzas();
                        totalTardanzas++;
                    } else {
                        resumenPorEmpleado.get(empleadoId).incrementarPuntualidades();
                        totalPuntualidades++;
                    }
                }
            }

            // Preparar la respuesta con totales generales y lista de empleados
            resultado.put("mes", mes);
            resultado.put("anio", anio);
            resultado.put("cantidadTardanzas", totalTardanzas);
            resultado.put("cantidadPuntualidades", totalPuntualidades);
            resultado.put("empleados", new ArrayList<>(resumenPorEmpleado.values()));

        } catch (IllegalArgumentException e) {
            resultado.put("error", "Error en los parámetros de entrada: " + e.getMessage());
        } catch (NullPointerException e) {
            resultado.put("error", "Error de datos: " + e.getMessage());
        } catch (Exception e) {
            resultado.put("error", "Error inesperado: " + e.getMessage());
        }

        return resultado;
    }

}