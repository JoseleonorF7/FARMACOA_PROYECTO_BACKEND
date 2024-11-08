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
    private Long id; // ID autoincremental del empleado

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre; // Nombre completo del empleado

    @Column(name = "identificacion", nullable = false, length = 50, unique = true)
    private String identificacion; // Identificación del empleado

    @Column(name = "fecha_contratacion", nullable = false)
    private LocalDate fechaContratacion; // Fecha de contratación

    @Column(name = "activo", nullable = false)
    private Boolean activo; // Estado de si el empleado sigue trabajando

    @Column(name = "huella_digital", length = 255)
    private String huellaDigital; // Hash o referencia de la huella dactilar (opcional)

    // Constructor vacío
    public Empleado_Model() {}

    // Constructor con parámetros
    public Empleado_Model(String nombre, String identificacion, LocalDate fechaContratacion, Boolean activo, String huellaDigital) {
        this.nombre = nombre;
        this.identificacion = identificacion;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
        this.huellaDigital = huellaDigital;
    }}