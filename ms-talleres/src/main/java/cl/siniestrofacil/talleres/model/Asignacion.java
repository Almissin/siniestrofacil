package cl.siniestrofacil.talleres.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Asignacion de una denuncia (por su folio) a un taller (relacion N:1 con Taller).
 */
@Entity
@Table(name = "asignaciones")
@Getter
@Setter
@NoArgsConstructor
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Folio de la denuncia en ms-denuncias (SF-AAAA-NNNNNN). Una denuncia se asigna una sola vez. */
    @Column(name = "folio_denuncia", nullable = false, unique = true, length = 20)
    private String folioDenuncia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAsignacion estado = EstadoAsignacion.ASIGNADA;

    @Column(name = "fecha_asignacion", nullable = false, updatable = false)
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;

    /** Dueno de la relacion: genera la columna taller_id (FK hacia talleres). */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "taller_id", nullable = false)
    private Taller taller;

    @PrePersist
    void alCrear() {
        this.fechaAsignacion = LocalDateTime.now();
    }
}
