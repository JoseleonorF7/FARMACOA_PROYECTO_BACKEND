package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.Horario_Model;
import Package.PHARMACY_PROJECT.Repository.Horario_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class Horario_Services {

    @Autowired
    private Horario_Repository horarioRepository;

    // Método para guardar un horario
    public Horario_Model saveHorario(Horario_Model horario) {
        return horarioRepository.save(horario);
    }

    // Método para obtener todos los horarios
    public List<Horario_Model> getAllHorarios() {
        return horarioRepository.findAll();
    }

    // Método para obtener un horario por su id
    public Optional<Horario_Model> getHorarioById(Long id) {
        return horarioRepository.findById(id);
    }

    // Método para eliminar un horario por su id
    public void deleteHorario(Long id) {
        horarioRepository.deleteById(id);
    }

    // Método para obtener horarios que coincidan con las horas de inicio o fin de un bloque
    public List<Horario_Model> getHorariosByBloques(java.time.LocalTime horaInicio, java.time.LocalTime horaFin) {
        return horarioRepository.findByHoraInicio1OrHoraFin1(horaInicio, horaFin);
    }
}
