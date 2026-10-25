package taller.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EstadoOrdenTest {
    @ParameterizedTest
    @CsvSource({
            "PENDIENTE, EN_PROCESO",
            "PENDIENTE, CANCELADA",
            "EN_PROCESO, FINALIZADA",
            "EN_PROCESO, CANCELADA"
    })
    void permiteLasTransicionesValidas(EstadoOrden desde, EstadoOrden hacia) {
        assertTrue(desde.puedeCambiarA(hacia));
    }

    @ParameterizedTest
    @CsvSource({
            "PENDIENTE, FINALIZADA",
            "PENDIENTE, PENDIENTE",
            "EN_PROCESO, PENDIENTE",
            "FINALIZADA, EN_PROCESO",
            "FINALIZADA, CANCELADA",
            "CANCELADA, PENDIENTE",
            "CANCELADA, EN_PROCESO"
    })
    void rechazaLasTransicionesInvalidas(EstadoOrden desde, EstadoOrden hacia) {
        assertFalse(desde.puedeCambiarA(hacia));
    }

    @Test
    void losEstadosFinalesNoPuedenCambiarANada() {
        for (EstadoOrden destino : EstadoOrden.values()) {
            assertFalse(EstadoOrden.FINALIZADA.puedeCambiarA(destino));
            assertFalse(EstadoOrden.CANCELADA.puedeCambiarA(destino));
        }
        assertEquals(4, EstadoOrden.values().length);
    }
}
