package Package.PHARMACY_PROJECT.Repository;

import Package.PHARMACY_PROJECT.Models.Horario_Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface Horario_Repository extends JpaRepository<Horario_Model, Long> {
    // Método para obtener un horario por su nombre
    List<Horario_Model> findByHoraInicio1OrHoraFin1(LocalTime horaInicio1, LocalTime horaFin1);
}