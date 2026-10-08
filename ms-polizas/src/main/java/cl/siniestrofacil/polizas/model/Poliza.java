package cl.siniestrofacil.polizas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Poliza de seguro automotriz. Una poliza incluye muchas coberturas (relacion 1:N con Cobertura).
 */
@Entity
@Table(name = "polizas")
@Getter
@Setter
@NoArgsConstructor
public class Poliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numero visible de la poliza, ej: POL-001. */
    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @Column(name = "rut_asegurado", nullable = false, length = 12)
    private String rutAsegurado;

    @Column(nullable = false, length = 6)
    private String patente;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_termino", nullable = false)
    private LocalDate fechaTermino;

    /** false = poliza anulada (no esta vigente aunque las fechas lo permitan). */
    @Column(nullable = false)
    private boolean activa = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /** Lado inverso. cascade + orphanRemoval: las coberturas se guardan y eliminan junto con la poliza. */
    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<Cobertura> coberturas = new ArrayList<>();

    @PrePersist
    void alCrear() {
        this.fechaCreacion = LocalDateTime.now();
    }

    /** Mantiene sincronizados ambos lados de la relacion. */
    public void agregarCobertura(Cobertura cobertura) {
        cobertura.setPoliza(this);
        this.coberturas.add(cobertura);
    }

    public void limpiarCoberturas() {
        this.coberturas.clear();
    }
}
