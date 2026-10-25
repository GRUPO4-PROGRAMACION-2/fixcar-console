package taller.dao.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import taller.exception.EntidadDuplicadaException;
import taller.exception.PersistenciaException;
import taller.model.Cliente;
import taller.model.Trabajador;
import taller.model.Vehiculo;
import taller.soporte.BaseDatosPrueba;
import taller.soporte.Fixtures;

class JdbcClienteYVehiculoDAOTest {
    private DataSource dataSource;
    private JdbcClienteDAO clienteDAO;
    private JdbcVehiculoDAO vehiculoDAO;

    @BeforeEach
    void preparar() {
        dataSource = BaseDatosPrueba.crear();
        clienteDAO = new JdbcClienteDAO(dataSource);
        vehiculoDAO = new JdbcVehiculoDAO(dataSource);
    }

    @AfterEach
    void limpiar() {
        dataSource.close();
    }

    @Test
    void guardaYRecuperaUnClienteConTodosSusDatos() {
        clienteDAO.guardar(Fixtures.cliente("DUI-1"));

        Cliente leido = clienteDAO.buscarPorId("DUI-1").orElseThrow();

        assertEquals("Ana Pérez", leido.getNombre());
        assertEquals("7000-1111", leido.getTelefono());
        assertEquals("ana@correo.com", leido.getCorreo());
        assertEquals("San Salvador", leido.getDireccion());
    }

    @Test
    void noPermiteDosClientesConElMismoDocumento() {
        clienteDAO.guardar(Fixtures.cliente("DUI-1"));

        assertThrows(EntidadDuplicadaException.class, () -> clienteDAO.guardar(Fixtures.cliente("DUI-1")));
    }

    @Test
    void listaLosClientesOrdenadosPorNombre() {
        clienteDAO.guardar(new Cliente("Zoila", "DUI-2", "7000", "z@correo.com", "x"));
        clienteDAO.guardar(new Cliente("Beto", "DUI-3", "7000", "b@correo.com", "x"));

        List<Cliente> clientes = clienteDAO.listarTodos();

        assertEquals(List.of("Beto", "Zoila"), clientes.stream().map(Cliente::getNombre).toList());
    }

    @Test
    void guardaYRecuperaUnVehiculoConSuDueno() {
        Cliente dueno = Fixtures.cliente("DUI-1");
        clienteDAO.guardar(dueno);
        vehiculoDAO.guardar(Fixtures.vehiculo(dueno, "ABC123"));

        Vehiculo leido = vehiculoDAO.buscarPorId("ABC123").orElseThrow();

        assertEquals("Toyota", leido.getMarca());
        assertEquals("DUI-1", leido.getCliente().getDocumento());
        assertEquals("Ana Pérez", leido.getCliente().getNombre());
    }

    @Test
    void noGuardaUnVehiculoDeUnClienteQueNoExiste() {
        Vehiculo huerfano = Fixtures.vehiculo(Fixtures.cliente("DUI-NO-EXISTE"), "ABC123");

        assertThrows(PersistenciaException.class, () -> vehiculoDAO.guardar(huerfano));
    }

    @Test
    void noPermiteDosVehiculosConLaMismaPlaca() {
        Cliente dueno = Fixtures.cliente("DUI-1");
        clienteDAO.guardar(dueno);
        vehiculoDAO.guardar(Fixtures.vehiculo(dueno, "ABC123"));

        assertThrows(EntidadDuplicadaException.class,
                () -> vehiculoDAO.guardar(Fixtures.vehiculo(dueno, "ABC123")));
    }

    @Test
    void losMecanicosInicialesVienenDeDataSql() {
        JdbcTrabajadorDAO trabajadorDAO = new JdbcTrabajadorDAO(dataSource);

        List<Trabajador> trabajadores = trabajadorDAO.listarTodos();

        assertEquals(List.of("Elisa", "Dilan"), trabajadores.stream().map(Trabajador::getNombre).toList());
        assertTrue(trabajadorDAO.existe("T-001"));
    }
}
