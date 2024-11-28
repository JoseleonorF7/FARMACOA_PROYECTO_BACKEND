package Package.PHARMACY_PROJECT.Services;
import Package.PHARMACY_PROJECT.Models.Usuario_Model;
import Package.PHARMACY_PROJECT.Repository.Usuario_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class Usuario_Services {

    private final Usuario_Repository usersRepository;

    public Usuario_Services(Usuario_Repository usersRepository) {
        this.usersRepository = usersRepository;
    }


    @Autowired
    private JavaMailSender mailSender;

    public List<Usuario_Model> findAll() {
        return usersRepository.findAll();
    }


    public Optional<Usuario_Model> findByUsername(String username) {

        return usersRepository.findByUsername(username);
    }


    public Optional<Usuario_Model> findByCorreoElectronico(String correoElectronico) {
        return usersRepository.findByCorreoElectronico(correoElectronico);
    }

    public Optional<Usuario_Model> findByToken(String token) {
        return usersRepository.findByToken(token);
    }


    public <S extends Usuario_Model> S save(S entity) {

        return usersRepository.save(entity);
    }


    public void delete(Usuario_Model entity) {

        usersRepository.delete(entity);
    }

    public void deleteById(Long aLong) {

        usersRepository.deleteById(aLong);
    }


    public Optional<Usuario_Model> findById(Long id) {

        return usersRepository.findById(id);
    }
}