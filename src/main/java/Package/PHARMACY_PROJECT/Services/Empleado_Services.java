package Package.PHARMACY_PROJECT.Services;
import Package.PHARMACY_PROJECT.Models.Empleado_Model;
import Package.PHARMACY_PROJECT.Models.Horario_Model;
import Package.PHARMACY_PROJECT.Repository.Empleado_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class Empleado_Services {

    private final Empleado_Repository empleadoRepository;

    @Autowired
    public Empleado_Services(Empleado_Repository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public List<Empleado_Model> getAllEmpleados() {
        return empleadoRepository.findAll();
    }
    // Método para guardar o actualizar un empleado
    public Empleado_Model save(Empleado_Model empleado) {
        return empleadoRepository.save(empleado);
    }

    // Método para obtener un horario por su id
    public Optional<Empleado_Model> getEmpleadoById(Long id) {
        return empleadoRepository.findById(id);
    }

    // Método para buscar un empleado por huella dactilar
    public Optional<Empleado_Model> findByHuellaDactilar(String huellaDactilar) {
        return empleadoRepository.findByHuellaDactilar(huellaDactilar);
    }
    // Método para obtener todas las huellas dactilares de todos los empleados
    public List<String> getAllHuellas() {
        return empleadoRepository.findAll()
                .stream()
                .map(Empleado_Model::getHuellaDactilar) // Obtén solo la huella dactilar
                .collect(Collectors.toList());
    }

    // Método para guardar un empleado con la huella dactilar
    public Empleado_Model saveHuella(Empleado_Model empleado) {
        // Guardar solo la huella dactilar
        Empleado_Model nuevoEmpleado = new Empleado_Model();
        nuevoEmpleado.setHuellaDactilar(empleado.getHuellaDactilar());
        // Guarda en la base de datos
        return empleadoRepository.save(nuevoEmpleado);
    }
    // Método para obtener la huella dactilar de un empleado por su ID
    public String getHuellaById(Long id) {
        Empleado_Model empleado = empleadoRepository.findById(id).orElse(null);
        if (empleado != null) {
            return empleado.getHuellaDactilar(); // devuelve la huella almacenada
        }
        return null;
    }

    public List<Empleado_Model> findAll() {
        return empleadoRepository.findAll();
    }

    public void deleteById(Long id) {
        empleadoRepository.deleteById(id);
    }

    public Optional<Empleado_Model> findByIdentificacion(String identificacion) {
        return empleadoRepository.findByIdentificacion(identificacion);
    }

    // Método para eliminar un empleado por identificación
    public boolean deleteByIdentificacion(String identificacion) {
        Optional<Empleado_Model> empleado = empleadoRepository.findByIdentificacion(identificacion);
        if (empleado.isPresent()) {
            empleadoRepository.delete(empleado.get());
            return true;
        }
        return false;  // Retorna false si no se encuentra el empleado
    }

    // Método para eliminar todos los empleados
    @Transactional
    public void deleteAll() {
        empleadoRepository.deleteAll();
        empleadoRepository.resetAutoIncrement();

    }

    public List<String> getHuellasSinIdentificacion() {
        // Buscar todos los empleados donde el campo 'identificacion' sea null o esté vacío
        List<Empleado_Model> empleadosSinIdentificacion = empleadoRepository.findByIdentificacionIsNullOrIdentificacion("");

        // Extraer solo las huellas de esos empleados
        List<String> huellas = empleadosSinIdentificacion.stream()
                .map(Empleado_Model::getHuellaDactilar)
                .collect(Collectors.toList());
        return huellas;
    }
    // Método para obtener el nombre del empleado según la identificación
    public Optional<String> getNombreByIdentificacion(String identificacion) {
        // Buscar el empleado por identificación
        Optional<Empleado_Model> empleadoOpt = empleadoRepository.findByIdentificacion(identificacion);

        // Si existe, devolver su nombre, de lo contrario, devolver un Optional vacío
        return empleadoOpt.map(Empleado_Model::getNombre);
    }

}

