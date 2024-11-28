package Package.PHARMACY_PROJECT.Repository;

import Package.PHARMACY_PROJECT.Models.Usuario_Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Usuario_Repository extends JpaRepository<Usuario_Model, Long> {

    Optional<Usuario_Model> findByUsername(String username);

    Optional<Usuario_Model> findByCorreoElectronico(String correoElectronico);

    Optional<Usuario_Model> findByToken(String token);


}