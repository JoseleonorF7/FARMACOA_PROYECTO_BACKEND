package Package.PHARMACY_PROJECT.Repository;

import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface Empleado_Repository extends JpaRepository<Empleado_Model, Long> {
    Optional<Empleado_Model> findByIdentificacion(String identificacion);
    Optional<Empleado_Model> findByHuellaDactilar(String huellaDactilar);
    List<Empleado_Model> findByIdentificacionIsNullOrIdentificacion(String identificacion);

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE empleados AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
}