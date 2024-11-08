package Package.PHARMACY_PROJECT.Repository;

import Package.PHARMACY_PROJECT.Models.Dueno_Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Dueno_Repository extends JpaRepository<Dueno_Model, Long> {
    Optional<Dueno_Model> findByUsername(String username);
}