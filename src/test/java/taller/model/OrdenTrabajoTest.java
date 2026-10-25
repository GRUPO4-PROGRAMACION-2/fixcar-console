package taller.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import taller.soporte.Fixtures;

class OrdenTrabajoTest {
    private final Cliente cliente = Fixtures.cliente("DUI-1");
    private final Vehiculo vehiculo = Fixtures.vehiculo(cliente, "ABC123");

    @Test
    void ordenNuevaArrancaPendienteYConFecha() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);

        assertEquals(EstadoOrden.PENDIENTE, orden.getEstado());
        assertNotNull(orden.getFechaCreacion());
        assertEquals("ABC123", orden.getVehiculo().getPlaca());
    }

    @Test
    void calculaElCostoTotalDeSusServicios() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);

        assertEquals(90.0, orden.calcularCostoTotal(), 0.001);
    }

    @Test
    void cambiaDeEstadoSiLaTransicionEsValida() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);

        orden.cambiarEstado(EstadoOrden.EN_PROCESO);
        orden.cambiarEstado(EstadoOrden.FINALIZADA);

        assertEquals(EstadoOrden.FINALIZADA, orden.getEstado());
    }

    @Test
    void lanzaExcepcionPropiaSiLaTransicionEsInvalida() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> orden.cambiarEstado(EstadoOrden.FINALIZADA));

        assertEquals("No se permite cambiar el estado de PENDIENTE a FINALIZADA", error.getMessage());
        assertEquals(EstadoOrden.PENDIENTE, orden.getEstado());
    }

    @Test
    void listaDeServiciosNoSePuedeModificarDesdeAfuera() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);

        assertThrows(UnsupportedOperationException.class,
                () -> orden.getServicios().add(new Mantenimiento("Extra", 5)));
        assertEquals(2, orden.getServicios().size());
    }

    @Test
    void rechazaDatosObligatoriosVacios() {
        Trabajador trabajador = Fixtures.elisa();

        assertThrows(IllegalArgumentException.class,
                () -> new OrdenTrabajo(" ", "desc", cliente, vehiculo, trabajador));
        assertThrows(IllegalArgumentException.class,
                () -> new OrdenTrabajo("O-1", "", cliente, vehiculo, trabajador));
        assertThrows(IllegalArgumentException.class,
                () -> new OrdenTrabajo("O-1", "desc", null, vehiculo, trabajador));
        assertThrows(IllegalArgumentException.class,
                () -> new OrdenTrabajo("O-1", "desc", cliente, null, trabajador));
        assertThrows(IllegalArgumentException.class,
                () -> new OrdenTrabajo("O-1", "desc", cliente, vehiculo, null));
    }

    @Test
    void rechazaServicioNulo() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);

        assertThrows(IllegalArgumentException.class, () -> orden.agregarServicio(null));
    }
}
