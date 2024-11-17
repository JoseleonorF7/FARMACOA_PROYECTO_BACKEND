package Package.PHARMACY_PROJECT.Repository;

import Package.PHARMACY_PROJECT.Models.TurnoProgramado_Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurnoProgramado_Repository extends JpaRepository<TurnoProgramado_Model, Long> {
    // Método para obtener todos los turnos programados
    List<TurnoProgramado_Model> findAll();

    // Método para obtener turnos por empleado
    List<TurnoProgramado_Model> findByEmpleadoId(Long id);

    // Método para obtener turnos por fecha
    List<TurnoProgramado_Model> findByFecha(java.time.LocalDate fecha);
}

