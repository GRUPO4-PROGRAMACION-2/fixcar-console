package taller.dao.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import taller.exception.EntidadDuplicadaException;
import taller.exception.PersistenciaException;
import taller.model.Cliente;
import taller.model.EstadoOrden;
import taller.model.Mantenimiento;
import taller.model.OrdenTrabajo;
import taller.model.Reparacion;
import taller.model.Servicio;
import taller.model.Trabajador;
import taller.model.Vehiculo;
import taller.soporte.BaseDatosPrueba;
import taller.soporte.Fixtures;

class JdbcOrdenDAOTest {
    private DataSource dataSource;
    private JdbcOrdenDAO ordenDAO;
    private Cliente cliente;
    private Vehiculo vehiculo;

    @BeforeEach
    void preparar() {
        dataSource = BaseDatosPrueba.crear();
        ordenDAO = new JdbcOrdenDAO(dataSource);

        cliente = Fixtures.cliente("DUI-1");
        vehiculo = Fixtures.vehiculo(cliente, "ABC123");
        new JdbcClienteDAO(dataSource).guardar(cliente);
        new JdbcVehiculoDAO(dataSource).guardar(vehiculo);
    }

    @AfterEach
    void limpiar() {
        dataSource.close();
    }

    @Test
    void guardaYRecuperaUnaOrdenConSusServicios() {
        ordenDAO.guardar(Fixtures.orden("O-1", cliente, vehiculo));

        OrdenTrabajo leida = ordenDAO.buscarPorId("O-1").orElseThrow();

        assertEquals("O-1", leida.getId());
        assertEquals(EstadoOrden.PENDIENTE, leida.getEstado());
        assertEquals("Ana Pérez", leida.getCliente().getNombre());
        assertEquals("ABC123", leida.getVehiculo().getPlaca());
        assertEquals("Elisa", leida.getTrabajador().getNombre());
        assertEquals(2, leida.getServicios().size());
        assertTrue(leida.getServicios().get(0) instanceof Mantenimiento);
        assertTrue(leida.getServicios().get(1) instanceof Reparacion);
        assertEquals(90.0, leida.calcularCostoTotal(), 0.001);
    }

    @Test
    void buscarUnaOrdenInexistenteDevuelveVacio() {
        assertEquals(Optional.empty(), ordenDAO.buscarPorId("O-404"));
        assertFalse(ordenDAO.existe("O-404"));
    }

    @Test
    void guardarUnIdRepetidoLanzaEntidadDuplicada() {
        ordenDAO.guardar(Fixtures.orden("O-1", cliente, vehiculo));

        assertThrows(EntidadDuplicadaException.class,
                () -> ordenDAO.guardar(Fixtures.orden("O-1", cliente, vehiculo)));
    }

    @Test
    void siFallaUnServicioLaTransaccionHaceRollbackYNoQuedaLaOrden() {
        OrdenTrabajo orden = new OrdenTrabajo("O-1", "Revisión", cliente, vehiculo, Fixtures.elisa());
        orden.agregarServicio(new Mantenimiento("Aceite", 30));
        orden.agregarServicio(new Mantenimiento("x".repeat(200), 10));

        assertThrows(PersistenciaException.class, () -> ordenDAO.guardar(orden));

        assertTrue(ordenDAO.buscarPorId("O-1").isEmpty(), "El rollback debe deshacer también la orden");
        assertTrue(ordenDAO.listarTodos().isEmpty());
    }

    @Test
    void unErrorInesperadoTambienDeshaceLaOrden() {
        OrdenTrabajo orden = new OrdenTrabajo("O-1", "Revisión", cliente, vehiculo, Fixtures.elisa());
        orden.agregarServicio(new Mantenimiento("Aceite", 30));
        orden.agregarServicio(new Mantenimiento("Filtro", 10) {
            @Override
            public String getTipo() {
                throw new IllegalStateException("falla inesperada");
            }
        });

        assertThrows(IllegalStateException.class, () -> ordenDAO.guardar(orden));

        assertTrue(ordenDAO.buscarPorId("O-1").isEmpty(), "la orden no debe quedar guardada sin sus servicios");
    }

    @Test
    void guardarConTrabajadorInexistenteFallaPorLlaveForanea() {
        Trabajador fantasma = new Trabajador("Nadie", "T-999", "7000", "Mecánico", "Nada");
        OrdenTrabajo orden = new OrdenTrabajo("O-1", "Revisión", cliente, vehiculo, fantasma);
        orden.agregarServicio(new Mantenimiento("Aceite", 30));

        assertThrows(PersistenciaException.class, () -> ordenDAO.guardar(orden));
    }

    @Test
    void actualizarEstadoSoloFuncionaSiElEstadoEsperadoCoincide() {
        ordenDAO.guardar(Fixtures.orden("O-1", cliente, vehiculo));

        boolean primero = ordenDAO.actualizarEstado("O-1", EstadoOrden.PENDIENTE, EstadoOrden.EN_PROCESO);
        boolean segundo = ordenDAO.actualizarEstado("O-1", EstadoOrden.PENDIENTE, EstadoOrden.CANCELADA);

        assertTrue(primero);
        assertFalse(segundo, "La segunda actualización ya no debe encontrar la orden en PENDIENTE");
        assertEquals(EstadoOrden.EN_PROCESO, ordenDAO.buscarPorId("O-1").orElseThrow().getEstado());
    }

    @Test
    void actualizarEstadoDeUnaOrdenInexistenteDevuelveFalse() {
        assertFalse(ordenDAO.actualizarEstado("O-404", EstadoOrden.PENDIENTE, EstadoOrden.EN_PROCESO));
    }

    @Test
    void filtraPorEstado() {
        ordenDAO.guardar(Fixtures.orden("O-1", cliente, vehiculo));
        ordenDAO.guardar(Fixtures.orden("O-2", cliente, vehiculo));
        ordenDAO.actualizarEstado("O-2", EstadoOrden.PENDIENTE, EstadoOrden.EN_PROCESO);

        List<OrdenTrabajo> pendientes = ordenDAO.listarPorEstado(EstadoOrden.PENDIENTE);

        assertEquals(1, pendientes.size());
        assertEquals("O-1", pendientes.get(0).getId());
    }

    @Test
    void ordenesConUnServicioSeLeenCompletas() {
        OrdenTrabajo orden = new OrdenTrabajo("O-1", "Revisión", cliente, vehiculo, Fixtures.elisa());
        Servicio unico = new Reparacion("Alternador", 100, 25.5);
        orden.agregarServicio(unico);
        ordenDAO.guardar(orden);

        Servicio leido = ordenDAO.buscarPorId("O-1").orElseThrow().getServicios().get(0);

        assertEquals("Alternador", leido.getNombre());
        assertEquals(125.5, leido.calcularCosto(), 0.001);
        assertEquals(25.5, leido.getCostoManoObra(), 0.001);
    }
}
