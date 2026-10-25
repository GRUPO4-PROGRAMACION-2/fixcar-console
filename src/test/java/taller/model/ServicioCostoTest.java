package taller.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class ServicioCostoTest {
    @Test
    void mantenimientoCuestaSoloElPrecioBase() {
        Servicio servicio = new Mantenimiento("Cambio de aceite", 30);

        assertEquals(30.0, servicio.calcularCosto(), 0.001);
        assertEquals("MANTENIMIENTO", servicio.getTipo());
        assertEquals(0.0, servicio.getCostoManoObra(), 0.001);
    }

    @Test
    void reparacionSumaLaManoDeObra() {
        Servicio servicio = new Reparacion("Pastillas", 50, 10);

        assertEquals(60.0, servicio.calcularCosto(), 0.001);
        assertEquals("REPARACION", servicio.getTipo());
        assertEquals(10.0, servicio.getCostoManoObra(), 0.001);
    }

    @Test
    void sumarServiciosDeDistintoTipoUsaElCalculoDeCadaUno() {
        List<Servicio> servicios = List.of(
                new Mantenimiento("Aceite", 30),
                new Reparacion("Frenos", 50, 10),
                new Reparacion("Luces", 20, 5));

        double total = servicios.stream().mapToDouble(Servicio::calcularCosto).sum();

        assertEquals(115.0, total, 0.001);
    }

    @Test
    void rechazaPreciosNegativosOInfinitos() {
        assertThrows(IllegalArgumentException.class, () -> new Mantenimiento("Aceite", -1));
        assertThrows(IllegalArgumentException.class, () -> new Mantenimiento("Aceite", Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new Reparacion("Frenos", 10, -5));
    }

    @Test
    void rechazaNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Mantenimiento("  ", 10));
    }
}
