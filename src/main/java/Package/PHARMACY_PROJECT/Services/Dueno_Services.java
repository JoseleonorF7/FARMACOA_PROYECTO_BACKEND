package Package.PHARMACY_PROJECT.Services;
import Package.PHARMACY_PROJECT.Models.Dueno_Model;
import Package.PHARMACY_PROJECT.Repository.Dueno_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class Dueno_Services {

    private final Dueno_Repository duenoRepository;

    @Autowired
    public Dueno_Services(Dueno_Repository duenoRepository) {
        this.duenoRepository = duenoRepository;
    }

    public Dueno_Model save(Dueno_Model dueno) {
        return duenoRepository.save(dueno);
    }

    public List<Dueno_Model> findAll() {
        return duenoRepository.findAll();
    }

    public void deleteById(Long id) {
        duenoRepository.deleteById(id);
    }

    public Optional<Dueno_Model> findByUsername(String username) {
        return duenoRepository.findByUsername(username);
    }
}