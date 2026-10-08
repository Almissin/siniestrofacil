package cl.siniestrofacil.polizas.service;

/** Resultado de evaluar la vigencia de una poliza: si esta vigente y por que. */
public record ResultadoVigencia(boolean vigente, String motivo) {
}
