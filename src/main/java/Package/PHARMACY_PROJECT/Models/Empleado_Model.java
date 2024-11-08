package Package.PHARMACY_PROJECT.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "empleados")
@Getter
@Setter
public class Empleado_Model {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ID autoincremental

    @Column(name = "nombre", length = 100, nullable = true)  // Asegúrate de que sea null
    private String nombre;  // Nombre completo del empleado

    @Column(name = "identificacion", length = 50, unique = true, nullable = true)  // Asegúrate de que sea null
    private String identificacion;  // Identificación del empleado

    @Column(name = "fecha_contratacion", nullable = true)  // Asegúrate de que sea null
    private LocalDate fechaContratacion;  // Fecha de contratación

    @Column(name = "activo", nullable = true)  // Asegúrate de que sea null
    private Boolean activo;  // Estado de si el empleado sigue trabajando

    @Column(name = "Rol", nullable = true)  // Ya permitido como null
    private String rol;

    @Column(name = "huella_dactilar", nullable = true)  // Ya permitido como null
    private String huellaDactilar;  // Huella dactilar del empleado

    // Constructor vacío
    public Empleado_Model() {
    }

    public Empleado_Model(Boolean activo, LocalDate fechaContratacion, String huellaDactilar, String identificacion, String nombre, String rol) {
        this.activo = activo;
        this.fechaContratacion = fechaContratacion;
        this.huellaDactilar = huellaDactilar;
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.rol = rol;
    }
}
