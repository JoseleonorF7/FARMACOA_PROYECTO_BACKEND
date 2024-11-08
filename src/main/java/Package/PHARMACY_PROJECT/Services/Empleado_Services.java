package Package.PHARMACY_PROJECT.Services;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Repository.Empleado_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class Empleado_Services {

    private final Empleado_Repository empleadoRepository;

    @Autowired
    public Empleado_Services(Empleado_Repository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public Empleado_Model save(Empleado_Model empleado) {
        return empleadoRepository.save(empleado);
    }

    public List<Empleado_Model> findAll() {
        return empleadoRepository.findAll();
    }

    public void deleteById(Long id) {
        empleadoRepository.deleteById(id);
    }

    public Optional<Empleado_Model> findByIdentificacion(String identificacion) {
        return empleadoRepository.findByIdentificacion(identificacion);
    }}