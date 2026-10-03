package cl.siniestrofacil.denuncias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// entidad que se guarda en la tabla "denuncias" de MySQL
@Entity
@Table(name = "denuncias")
// generar gett
@Getter
// generar sett
@Setter
public class Denuncia {

    // el folio es la llave primaria
    @Id
    @Column(name = "folio", length = 20)
    private String folio;

    @Column(name = "patente", length = 6, nullable = false)
    private String patente;

    @Column(name = "rut_asegurado", length = 12, nullable = false)
    private String rutAsegurado;

    @Column(name = "numero_poliza", length = 50, nullable = false)
    private String numeroPoliza;

    @Column(name = "fecha_siniestro", nullable = false)
    private LocalDate fechaSiniestro;

    @Column(name = "descripcion", length = 500, nullable = false)
    private String descripcion;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
}
