package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Repository.Asistencia_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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



}