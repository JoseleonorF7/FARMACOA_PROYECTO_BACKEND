package Package.PHARMACY_PROJECT.Repository;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface Asistencia_Repository extends JpaRepository<Asistencia_Model, Long> {
    List<Asistencia_Model> findByEmpleadoId(Long empleadoId);
    // Método para encontrar asistencia por empleado y fecha
    Optional<Asistencia_Model> findByEmpleadoAndFecha(Empleado_Model empleado, LocalDate fecha);
    Optional<Asistencia_Model> findByEmpleadoAndFechaAndTipoRegistro(Empleado_Model empleado, LocalDate fecha, String tipoRegistro);
    public List<Asistencia_Model> findByEmpleadoIdAndFecha(Long empleadoId, LocalDate fecha);

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE asistencias AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();

    Optional<Asistencia_Model> findTopByEmpleadoOrderByHoraEntradaDesc(Empleado_Model empleado);

    @Query("SELECT a FROM Asistencia_Model a WHERE MONTH(a.fecha) = :mes")
    public List<Asistencia_Model> findByMes(@Param("mes") Integer mes);

}