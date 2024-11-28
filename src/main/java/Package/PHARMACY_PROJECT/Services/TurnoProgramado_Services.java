package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.TurnoProgramado_Model;
import Package.PHARMACY_PROJECT.Models.Horario_Model;
import Package.PHARMACY_PROJECT.Repository.Empleado_Repository;
import Package.PHARMACY_PROJECT.Repository.TurnoProgramado_Repository;
import Package.PHARMACY_PROJECT.Repository.Horario_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TurnoProgramado_Services {

    @Autowired
    private TurnoProgramado_Repository turnoProgramadoRepository;

    @Autowired
    private Horario_Repository horarioRepository;

    @Autowired
    private Empleado_Repository empleadoRepository;

    // Método para guardar un horario
    public Horario_Model saveHorario(Horario_Model horario) {
        return horarioRepository.save(horario);
    }

    // Método para obtener todos los horarios
    public List<Horario_Model> getAllHorarios() {
        return horarioRepository.findAll();
    }

    // Método para guardar un turno programado
    public TurnoProgramado_Model saveTurnoProgramado(TurnoProgramado_Model turnoProgramado) {
        return turnoProgramadoRepository.save(turnoProgramado);
    }

    // Método para obtener todos los turnos programados
    public List<TurnoProgramado_Model> getAllTurnosProgramados() {
        return turnoProgramadoRepository.findAll();
    }



    // Método para obtener los turnos programados por fecha
    public List<TurnoProgramado_Model> getTurnosByFecha(java.time.LocalDate fecha) {
        return turnoProgramadoRepository.findByFecha(fecha);
    }

    // Método para obtener un horario por su ID
    public Horario_Model getHorarioById(Long id) {
        return horarioRepository.findById(id).orElse(null);
    }

    // Método para obtener un empleado por su ID
    public Empleado_Model getEmpleadoById(Long empleadoId) {
        return empleadoRepository.findById(empleadoId).orElse(null);
    }


}