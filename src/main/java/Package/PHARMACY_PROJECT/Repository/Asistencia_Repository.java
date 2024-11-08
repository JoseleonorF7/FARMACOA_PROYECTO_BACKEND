package Package.PHARMACY_PROJECT.Repository;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Asistencia_Repository extends JpaRepository<Asistencia_Model, Long> {
    List<Asistencia_Model> findByEmpleadoId(Long empleadoId);
}