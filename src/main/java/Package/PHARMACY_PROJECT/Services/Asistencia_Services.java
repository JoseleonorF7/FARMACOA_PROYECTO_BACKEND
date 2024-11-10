package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.Asistencia_Model;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Repository.Asistencia_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class Asistencia_Services {

    private final Asistencia_Repository asistenciaRepository;

    @Autowired
    public Asistencia_Services(Asistencia_Repository asistenciaRepository) {
        this.asistenciaRepository = asistenciaRepository;
    }

    // Método para encontrar asistencia por empleado y fecha
    public Optional<Asistencia_Model> findByEmpleadoAndFecha(Empleado_Model empleado, LocalDate fecha) {
        return asistenciaRepository.findByEmpleadoAndFecha(empleado, fecha);
    }

    public Optional<Asistencia_Model> findByEmpleadoAndFechaAndTipoRegistro(Empleado_Model empleado, LocalDate fecha,String tipoRegistro) {
        return asistenciaRepository.findByEmpleadoAndFechaAndTipoRegistro(empleado, fecha,tipoRegistro);
    }

    // Método para guardar la asistencia
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