package taller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import taller.dao.jdbc.JdbcClienteDAO;
import taller.dao.jdbc.JdbcOrdenDAO;
import taller.dao.jdbc.JdbcTrabajadorDAO;
import taller.dao.jdbc.JdbcVehiculoDAO;
import taller.dto.ResultadoDiagnostico;
import taller.dto.ResumenLoteDTO;
import taller.model.Cliente;
import taller.model.EstadoOrden;
import taller.model.OrdenTrabajo;
import taller.model.Vehiculo;
import taller.soporte.BaseDatosPrueba;
import taller.soporte.Fixtures;

// concurrencia contra una base H2 real: varios hilos, conexiones del pool
class ProcesadorOrdenesTest {

    private DataSource dataSource;
    private TallerService service;
    private ProcesadorOrdenes procesador;
    private Cliente cliente;
    private Vehiculo vehiculo;

    @BeforeEach
    void preparar() {
        dataSource = BaseDatosPrueba.crear();
        service = new TallerService(new JdbcClienteDAO(dataSource), new JdbcVehiculoDAO(dataSource),
                new JdbcTrabajadorDAO(dataSource), new JdbcOrdenDAO(dataSource));
        procesador = new ProcesadorOrdenes(service, 4, 20);

        cliente = Fixtures.cliente("DUI-1");
        vehiculo = Fixtures.vehiculo(cliente, "ABC123");
        service.registrarCliente(cliente);
        service.registrarVehiculo(vehiculo);
    }

    @AfterEach
    void limpiar() {
        procesador.close();
        dataSource.close();
    }

    private void crearOrdenes(int cantidad) {
        for (int i = 1; i <= cantidad; i++) {
            service.crearOrden(Fixtures.orden("O-" + i, cliente, vehiculo));
        }
    }

    private EstadoOrden estadoDe(String id) {
        return service.listarOrdenes().stream()
                .filter(o -> o.getId().equals(id))
                .map(OrdenTrabajo::getEstado)
                .findFirst().orElseThrow();
    }

    @Test
    void diagnosticaVeinteOrdenesEnParaleloSinPerderNinguna() {
        crearOrdenes(20);

        ResumenLoteDTO resumen = procesador.diagnosticarPendientes();

        assertEquals(20, resumen.total());
        assertEquals(20, resumen.exitosos());
        assertEquals(0, resumen.fallidos());
        assertEquals(20, service.listarOrdenes().stream()
                .filter(o -> o.getEstado() == EstadoOrden.EN_PROCESO).count());
        assertEquals(0, dataSource.getActive(), "todas las conexiones deben volver al pool");
    }

    @Test
    void usaVariosHilosDistintos() {
        crearOrdenes(12);

        ResumenLoteDTO resumen = procesador.diagnosticarPendientes();

        long hilosUsados = resumen.detalle().stream().map(ResultadoDiagnostico::hilo).distinct().count();
        assertTrue(hilosUsados > 1);
        assertTrue(resumen.detalle().stream().allMatch(r -> r.hilo().startsWith("procesador-ordenes-")));
    }

    @Test
    void cuandoMuchosHilosDiagnosticanLaMismaOrdenSoloUnoGana() throws Exception {
        crearOrdenes(1);

        List<Future<ResultadoDiagnostico>> futuros = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            futuros.add(procesador.encolarDiagnostico("O-1", EstadoOrden.EN_PROCESO));
        }
        List<ResultadoDiagnostico> resultados = new ArrayList<>();
        for (Future<ResultadoDiagnostico> futuro : futuros) {
            resultados.add(futuro.get());
        }

        assertEquals(1, resultados.stream().filter(ResultadoDiagnostico::exitoso).count());
        assertEquals(11, resultados.stream().filter(r -> !r.exitoso()).count());
        assertEquals(EstadoOrden.EN_PROCESO, estadoDe("O-1"));
    }

    @Test
    void unaOrdenInexistenteDevuelveFalloSinAfectarAlResto() throws Exception {
        crearOrdenes(1);

        ResultadoDiagnostico malo = procesador.encolarDiagnostico("O-99", EstadoOrden.EN_PROCESO).get();
        ResultadoDiagnostico bueno = procesador.encolarDiagnostico("O-1", EstadoOrden.EN_PROCESO).get();

        assertFalse(malo.exitoso());
        assertTrue(malo.mensaje().contains("O-99"));
        assertTrue(bueno.exitoso());
    }

    @Test
    void unaTransicionInvalidaFallaConElMotivo() throws Exception {
        crearOrdenes(1);

        ResultadoDiagnostico resultado = procesador.encolarDiagnostico("O-1", EstadoOrden.FINALIZADA).get();

        assertFalse(resultado.exitoso());
        assertTrue(resultado.mensaje().contains("PENDIENTE"));
        assertEquals(EstadoOrden.PENDIENTE, estadoDe("O-1"));
    }

    @Test
    void lasNotificacionesSeEntreganUnaSolaVez() throws Exception {
        crearOrdenes(3);
        for (int i = 1; i <= 3; i++) {
            procesador.encolarDiagnostico("O-" + i, EstadoOrden.EN_PROCESO).get();
        }

        assertEquals(3, procesador.retirarNotificaciones().size());
        assertTrue(procesador.retirarNotificaciones().isEmpty());
    }

    @Test
    void sinOrdenesPendientesElLoteVieneVacio() {
        ResumenLoteDTO resumen = procesador.diagnosticarPendientes();

        assertEquals(0, resumen.total());
        assertTrue(resumen.detalle().isEmpty());
    }

    @Test
    void validaSusParametros() {
        assertThrows(IllegalArgumentException.class, () -> procesador.encolarDiagnostico(" ", EstadoOrden.EN_PROCESO));
        assertThrows(IllegalArgumentException.class, () -> new ProcesadorOrdenes(service, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new ProcesadorOrdenes(service, 2, -1));
        assertThrows(NullPointerException.class, () -> new ProcesadorOrdenes(null));
    }
}
