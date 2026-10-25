package taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import taller.dao.ClienteDAO;
import taller.dao.OrdenDAO;
import taller.dao.TrabajadorDAO;
import taller.dao.VehiculoDAO;
import taller.exception.ConcurrenciaException;
import taller.exception.EntidadDuplicadaException;
import taller.exception.EntidadNoEncontradaException;
import taller.exception.TransicionEstadoInvalidaException;
import taller.model.Cliente;
import taller.model.EstadoOrden;
import taller.model.OrdenTrabajo;
import taller.model.Vehiculo;
import taller.soporte.Fixtures;

@ExtendWith(MockitoExtension.class)
class TallerServiceTest {

    @Mock
    private ClienteDAO clienteDAO;
    @Mock
    private VehiculoDAO vehiculoDAO;
    @Mock
    private TrabajadorDAO trabajadorDAO;
    @Mock
    private OrdenDAO ordenDAO;

    private TallerService service;
    private Cliente cliente;
    private Vehiculo vehiculo;

    @BeforeEach
    void preparar() {
        service = new TallerService(clienteDAO, vehiculoDAO, trabajadorDAO, ordenDAO);
        cliente = Fixtures.cliente("DUI-1");
        vehiculo = Fixtures.vehiculo(cliente, "ABC123");
    }

    @Test
    void registraUnClienteNuevo() {
        when(clienteDAO.existe("DUI-1")).thenReturn(false);

        service.registrarCliente(cliente);

        verify(clienteDAO).guardar(cliente);
    }

    @Test
    void noRegistraUnClienteConDocumentoRepetido() {
        when(clienteDAO.existe("DUI-1")).thenReturn(true);

        assertThrows(EntidadDuplicadaException.class, () -> service.registrarCliente(cliente));

        verify(clienteDAO, never()).guardar(any());
    }

    @Test
    void rechazaClienteNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarCliente(null));
    }

    @Test
    void noRegistraVehiculoDeUnClienteQueNoExiste() {
        when(clienteDAO.existe("DUI-1")).thenReturn(false);

        assertThrows(EntidadNoEncontradaException.class, () -> service.registrarVehiculo(vehiculo));

        verify(vehiculoDAO, never()).guardar(any());
    }

    @Test
    void noRegistraUnaPlacaRepetida() {
        when(clienteDAO.existe("DUI-1")).thenReturn(true);
        when(vehiculoDAO.existe("ABC123")).thenReturn(true);

        assertThrows(EntidadDuplicadaException.class, () -> service.registrarVehiculo(vehiculo));

        verify(vehiculoDAO, never()).guardar(any());
    }

    @Test
    void registraUnVehiculoValido() {
        when(clienteDAO.existe("DUI-1")).thenReturn(true);
        when(vehiculoDAO.existe("ABC123")).thenReturn(false);

        service.registrarVehiculo(vehiculo);

        verify(vehiculoDAO).guardar(vehiculo);
    }

    @Test
    void creaUnaOrdenValida() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);
        when(ordenDAO.existe("O-1")).thenReturn(false);
        when(vehiculoDAO.buscarPorId("ABC123")).thenReturn(Optional.of(vehiculo));
        when(trabajadorDAO.existe("T-001")).thenReturn(true);

        service.crearOrden(orden);

        verify(ordenDAO).guardar(orden);
    }

    @Test
    void noCreaUnaOrdenConIdRepetido() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);
        when(ordenDAO.existe("O-1")).thenReturn(true);

        assertThrows(EntidadDuplicadaException.class, () -> service.crearOrden(orden));

        verify(ordenDAO, never()).guardar(any());
    }

    @Test
    void noCreaUnaOrdenConVehiculoQueNoExiste() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);
        when(ordenDAO.existe("O-1")).thenReturn(false);
        when(vehiculoDAO.buscarPorId("ABC123")).thenReturn(Optional.empty());

        assertThrows(EntidadNoEncontradaException.class, () -> service.crearOrden(orden));
    }

    @Test
    void noCreaUnaOrdenConVehiculoDeOtroCliente() {
        Vehiculo deOtro = Fixtures.vehiculo(Fixtures.cliente("DUI-2"), "ABC123");
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);
        when(ordenDAO.existe("O-1")).thenReturn(false);
        when(vehiculoDAO.buscarPorId("ABC123")).thenReturn(Optional.of(deOtro));

        assertThrows(IllegalArgumentException.class, () -> service.crearOrden(orden));

        verify(ordenDAO, never()).guardar(any());
    }

    @Test
    void cambiaElEstadoCuandoLaTransicionEsValida() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);
        when(ordenDAO.buscarPorId("O-1")).thenReturn(Optional.of(orden));
        when(ordenDAO.actualizarEstado("O-1", EstadoOrden.PENDIENTE, EstadoOrden.EN_PROCESO)).thenReturn(true);

        service.cambiarEstadoOrden("O-1", EstadoOrden.EN_PROCESO);

        verify(ordenDAO).actualizarEstado("O-1", EstadoOrden.PENDIENTE, EstadoOrden.EN_PROCESO);
    }

    @Test
    void noActualizaLaBaseSiLaTransicionEsInvalida() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);
        when(ordenDAO.buscarPorId("O-1")).thenReturn(Optional.of(orden));

        assertThrows(TransicionEstadoInvalidaException.class,
                () -> service.cambiarEstadoOrden("O-1", EstadoOrden.FINALIZADA));

        verify(ordenDAO, never()).actualizarEstado(any(), any(), any());
    }

    @Test
    void avisaSiOtroHiloModificoLaOrdenPrimero() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);
        when(ordenDAO.buscarPorId("O-1")).thenReturn(Optional.of(orden));
        when(ordenDAO.actualizarEstado("O-1", EstadoOrden.PENDIENTE, EstadoOrden.EN_PROCESO)).thenReturn(false);

        assertThrows(ConcurrenciaException.class,
                () -> service.cambiarEstadoOrden("O-1", EstadoOrden.EN_PROCESO));
    }

    @Test
    void fallaSiLaOrdenNoExiste() {
        when(ordenDAO.buscarPorId("O-99")).thenReturn(Optional.empty());

        EntidadNoEncontradaException error = assertThrows(EntidadNoEncontradaException.class,
                () -> service.procesarDiagnostico("O-99", EstadoOrden.EN_PROCESO));

        assertTrue(error.getMessage().contains("O-99"));
    }

    @Test
    void exigeIdYEstadoAlCambiarElEstado() {
        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstadoOrden(" ", EstadoOrden.EN_PROCESO));
        assertThrows(IllegalArgumentException.class, () -> service.cambiarEstadoOrden("O-1", null));
    }

    @Test
    void listaSoloLasOrdenesPendientes() {
        OrdenTrabajo orden = Fixtures.orden("O-1", cliente, vehiculo);
        when(ordenDAO.listarPorEstado(EstadoOrden.PENDIENTE)).thenReturn(java.util.List.of(orden));

        assertEquals(1, service.listarOrdenesPendientes().size());
    }

    @Test
    void exigeTodosLosDao() {
        assertThrows(IllegalArgumentException.class,
                () -> new TallerService(null, vehiculoDAO, trabajadorDAO, ordenDAO));
        assertThrows(IllegalArgumentException.class,
                () -> new TallerService(clienteDAO, vehiculoDAO, trabajadorDAO, null));
    }
}
