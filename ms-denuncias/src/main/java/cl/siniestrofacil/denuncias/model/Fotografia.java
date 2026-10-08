package cl.siniestrofacil.denuncias.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Referencia a una fotografia del siniestro (relacion N:1 con Denuncia).
 * Se guarda la URL, no la imagen (en AWS las imagenes estaran en Amazon S3).
 */
@Entity
@Table(name = "fotografias")
@Getter
@Setter
@NoArgsConstructor
public class Fotografia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 150)
    private String descripcion;

    /** Dueno de la relacion: genera la columna denuncia_folio (FK hacia denuncias.folio). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "denuncia_folio", nullable = false)
    private Denuncia denuncia;

    public Fotografia(String url, String descripcion) {
        this.url = url;
        this.descripcion = descripcion;
    }
}
