package Package.PHARMACY_PROJECT.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "duenos")
@Getter
@Setter
public class Dueno_Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ID autoincremental del dueño

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username; // Nombre de usuario

    @Column(name = "password", nullable = false, length = 255)
    private String password; // Contraseña del dueño

    @Column(name = "huella_digital", length = 255)
    private String huellaDigital; // Hash o referencia de la huella dactilar (opcional)

    // Constructor vacío
    public Dueno_Model() {}

    // Constructor con parámetros
    public Dueno_Model(String username, String password, String huellaDigital) {
        this.username = username;
        this.password = password;
        this.huellaDigital = huellaDigital;
    }}