package cl.siniestrofacil.denuncias.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// generar gett
@Getter
// generar sett
@Setter
public class Denuncia {

    private String folio;
    private String patente;
    private String rutAsegurado;
    private String numeroPoliza;
    private LocalDate fechaSiniestro;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaRegistro;
}