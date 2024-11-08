package Package.PHARMACY_PROJECT.Services;

import Package.PHARMACY_PROJECT.Models.InformeAsistencia_Model;
import Package.PHARMACY_PROJECT.Repository.InformeAsistencia_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InformeAsistencia_Services {

    private final InformeAsistencia_Repository informeAsistenciaRepository;

    @Autowired
    public InformeAsistencia_Services(InformeAsistencia_Repository informeAsistenciaRepository) {
        this.informeAsistenciaRepository = informeAsistenciaRepository;
    }

    public InformeAsistencia_Model save(InformeAsistencia_Model informe) {
        return informeAsistenciaRepository.save(informe);
    }

    public List<InformeAsistencia_Model> findAll() {
        return informeAsistenciaRepository.findAll();
    }

    public void deleteById(Long id) {
        informeAsistenciaRepository.deleteById(id);
    }
}