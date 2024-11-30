package Package.PHARMACY_PROJECT.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario_Model {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)// esto es para que el id se encrementre
    private long id;
    private String username;
    private String password;
    private String nombreCompleto;
    private String correoElectronico;
    private String rol; //ADMIN o usuario
    private String token;

    // Constructor vacío
    public Usuario_Model() {
    }

    public Usuario_Model(String correoElectronico, String nombreCompleto, String password, String rol, String token, String username) {
        this.correoElectronico = correoElectronico;
        this.nombreCompleto = nombreCompleto;
        this.password = password;
        this.rol = rol;
        this.token = token;
        this.username = username;
    }
}