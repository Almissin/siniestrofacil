package cl.siniestrofacil.usuarios.model;

/**
 * Roles del sistema SiniestroFacil.
 * ADMIN: administra usuarios, polizas y talleres.
 * ASEGURADO: registra y consulta sus propias denuncias.
 * TALLER: consulta las denuncias asignadas a su taller y confirma la recepcion del vehiculo.
 */
public enum NombreRol {
    ADMIN,
    ASEGURADO,
    TALLER
}
