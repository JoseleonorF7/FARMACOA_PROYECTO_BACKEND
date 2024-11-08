package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Repository.Asistencia_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Asistencia_Services {

    private final Asistencia_Repository asistenciaRepository;

    @Autowired
    public Asistencia_Services(Asistencia_Repository asistenciaRepository) {
        this.asistenciaRepository = asistenciaRepository;
    }

    public Asistencia_Model save(Asistencia_Model asistencia) {
        return asistenciaRepository.save(asistencia);
    }

    public List<Asistencia_Model> findAll() {
        return asistenciaRepository.findAll();
    }

    public void deleteById(Long id) {
        asistenciaRepository.deleteById(id);
    }

    public List<Asistencia_Model> findByEmpleadoId(Long empleadoId) {
        return asistenciaRepository.findByEmpleadoId(empleadoId);
    }}