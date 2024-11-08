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

    @Column(name = "huella_dactilar", nullable = true)  // Ya permitido como null
    private String huellaDactilar;  // Huella dactilar del empleado

    // Constructor vacío
    public Empleado_Model() {}

    // Constructor con parámetros
    public Empleado_Model(String nombre, String identificacion, LocalDate fechaContratacion, Boolean activo, String huellaDactilar) {
        this.nombre = nombre;
        this.identificacion = identificacion;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
        this.huellaDactilar = huellaDactilar;
    }}