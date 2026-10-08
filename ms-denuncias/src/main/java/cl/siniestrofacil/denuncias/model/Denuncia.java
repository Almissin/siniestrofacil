package cl.siniestrofacil.denuncias.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Denuncia de siniestro vehicular. Una denuncia tiene muchas fotografias (relacion 1:N con Fotografia).
 * El folio es la llave primaria (igual que en la version 1).
 */
@Entity
@Table(name = "denuncias")
@Getter
@Setter
@NoArgsConstructor
public class Denuncia {

    /** Folio de recepcion: SF-AAAA-NNNNNN. */
    @Id
    @Column(length = 20)
    private String folio;

    @Column(nullable = false, length = 6)
    private String patente;

    /** Rut del asegurado: se toma del token, no del cuerpo de la peticion. */
    @Column(name = "rut_asegurado", nullable = false, length = 12)
    private String rutAsegurado;

    @Column(name = "numero_poliza", nullable = false, length = 20)
    private String numeroPoliza;

    @Column(name = "fecha_siniestro", nullable = false)
    private LocalDate fechaSiniestro;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDenuncia estado = EstadoDenuncia.RECIBIDA;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    // ---------- resultado del procesamiento asincrono (EP3: AWS Lambda) ----------

    @Column(name = "taller_id")
    private Long tallerId;

    @Column(name = "nombre_taller", length = 100)
    private String nombreTaller;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    @Column(name = "fecha_resultado")
    private LocalDateTime fechaResultado;

    // ---------- recepcion del vehiculo por el taller ----------

    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    /** Lado inverso. cascade: las fotografias se guardan junto con la denuncia. */
    @OneToMany(mappedBy = "denuncia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<Fotografia> fotografias = new ArrayList<>();

    @PrePersist
    void alCrear() {
        this.fechaRegistro = LocalDateTime.now();
    }

    /** Mantiene sincronizados ambos lados de la relacion. */
    public void agregarFotografia(Fotografia fotografia) {
        fotografia.setDenuncia(this);
        this.fotografias.add(fotografia);
    }
}
