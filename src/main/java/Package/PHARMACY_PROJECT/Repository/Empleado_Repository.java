package Package.PHARMACY_PROJECT.Repository;

import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Empleado_Repository extends JpaRepository<Empleado_Model, Long> {
    Optional<Empleado_Model> findByIdentificacion(String identificacion);
    Optional<Empleado_Model> findByHuellaDactilar(String huellaDactilar);

}