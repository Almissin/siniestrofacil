package cl.siniestrofacil.talleres.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Taller en convenio. Un taller recibe muchas asignaciones (relacion 1:N con Asignacion).
 */
@Entity
@Table(name = "talleres")
@Getter
@Setter
@NoArgsConstructor
public class Taller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 12)
    private String rut;

    @Column(nullable = false, length = 150)
    private String direccion;

    @Column(nullable = false, length = 60)
    private String comuna;

    @Column(length = 20)
    private String telefono;

    @Column(length = 120)
    private String email;

    /** Cantidad maxima de vehiculos que el taller puede atender al mismo tiempo. */
    @Column(name = "cupo_maximo", nullable = false)
    private int cupoMaximo;

    /** Un taller inactivo no recibe nuevas asignaciones. */
    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Lado inverso: la llave foranea taller_id vive en la tabla asignaciones. */
    @OneToMany(mappedBy = "taller")
    private List<Asignacion> asignaciones = new ArrayList<>();

    @PrePersist
    void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
