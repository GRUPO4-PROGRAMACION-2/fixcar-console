package taller.dto;

import java.util.List;

public record ResumenLoteDTO(int total, int exitosos, int fallidos, long duracionMs, int hilos,
                             List<ResultadoDiagnostico> detalle) {
}
