package taller.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import taller.model.Cliente;
import taller.model.EstadoOrden;
import taller.model.Mantenimiento;
import taller.model.OrdenTrabajo;
import taller.model.Servicio;
import taller.model.Vehiculo;
import taller.service.ProcesadorOrdenes;
import taller.service.TallerService;
import taller.soporte.Fixtures;

@ExtendWith(MockitoExtension.class)
class TallerControllerTest {

    @Mock
    private TallerService service;
    @Mock
    private ProcesadorOrdenes procesador;

    private TallerController controller;

    @BeforeEach
    void preparar() {
        controller = new TallerController(service, procesador);
    }

    @Test
    void registrarClienteConstruyeElObjetoYLoGuardaPorElServicio() {
        Cliente cliente = controller.registrarCliente("Ana", "DUI-1", "7000", "ana@correo.com", "Centro");

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(service).registrarCliente(captor.capture());
        assertSame(cliente, captor.getValue());
        assertEquals("DUI-1", captor.getValue().getDocumento());
    }

    @Test
    void registrarClienteConCorreoInvalidoNoLlegaAlServicio() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.registrarCliente("Ana", "DUI-1", "7000", "malo", "Centro"));

        verifyNoInteractions(service);
    }

    @Test
    void crearOrdenAgregaTodosLosServiciosAntesDeGuardar() {
        Cliente cliente = Fixtures.cliente("DUI-1");
        Vehiculo vehiculo = Fixtures.vehiculo(cliente, "ABC123");
        List<Servicio> servicios = List.of(new Mantenimiento("Aceite", 30), new Mantenimiento("Filtro", 20));

        OrdenTrabajo orden = controller.crearOrden("O-1", "Revisión", cliente, vehiculo, Fixtures.elisa(), servicios);

        assertEquals(2, orden.getServicios().size());
        assertEquals(50.0, orden.calcularCostoTotal(), 0.001);
        verify(service).crearOrden(orden);
    }

    @Test
    void cambiarEstadoYEncolarDiagnosticoDelegan() {
        controller.cambiarEstadoOrden("O-1", EstadoOrden.CANCELADA);
        controller.encolarDiagnostico("O-2", EstadoOrden.EN_PROCESO);

        verify(service).cambiarEstadoOrden("O-1", EstadoOrden.CANCELADA);
        verify(procesador).encolarDiagnostico("O-2", EstadoOrden.EN_PROCESO);
    }

    @Test
    void closeCierraElProcesador() {
        controller.close();

        verify(procesador).close();
    }

    @Test
    void exigeSusDependencias() {
        assertThrows(IllegalArgumentException.class, () -> new TallerController(null, procesador));
        assertThrows(IllegalArgumentException.class, () -> new TallerController(service, null));
    }
}
