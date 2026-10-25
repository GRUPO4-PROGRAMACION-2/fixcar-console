package taller.dto;

public record ResultadoDiagnostico(String idOrden, boolean exitoso, String mensaje,
                                   long duracionMs, String hilo) {
}
