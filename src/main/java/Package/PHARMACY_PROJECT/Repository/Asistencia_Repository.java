package Package.PHARMACY_PROJECT.Repository;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface Asistencia_Repository extends JpaRepository<Asistencia_Model, Long> {
    List<Asistencia_Model> findByEmpleadoId(Long empleadoId);
    // Método para encontrar asistencia por empleado y fecha
    Optional<Asistencia_Model> findByEmpleadoAndFecha(Empleado_Model empleado, LocalDate fecha);
    Optional<Asistencia_Model> findByEmpleadoAndFechaAndTipoRegistro(Empleado_Model empleado, LocalDate fecha, String tipoRegistro);

}