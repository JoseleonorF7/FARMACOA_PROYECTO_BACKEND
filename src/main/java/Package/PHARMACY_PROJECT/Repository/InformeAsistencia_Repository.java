package Package.PHARMACY_PROJECT.Repository;

import Package.PHARMACY_PROJECT.Models.InformeAsistencia_Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InformeAsistencia_Repository extends JpaRepository<InformeAsistencia_Model, Long> {
}