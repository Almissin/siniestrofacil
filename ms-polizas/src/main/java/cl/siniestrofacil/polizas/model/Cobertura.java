package cl.siniestrofacil.polizas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cobertura incluida en una poliza (relacion N:1 con Poliza).
 */
@Entity
@Table(name = "coberturas")
@Getter
@Setter
@NoArgsConstructor
public class Cobertura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    /** Monto maximo cubierto, en pesos chilenos. */
    @Column(name = "monto_maximo", nullable = false)
    private Long montoMaximo;

    /** Dueno de la relacion: genera la columna poliza_id (FK hacia polizas). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poliza_id", nullable = false)
    private Poliza poliza;

    public Cobertura(String nombre, Long montoMaximo) {
        this.nombre = nombre;
        this.montoMaximo = montoMaximo;
    }
}
