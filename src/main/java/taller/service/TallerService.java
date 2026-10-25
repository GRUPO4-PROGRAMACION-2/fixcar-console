package taller.service;

import java.util.List;

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
import taller.model.Trabajador;
import taller.model.Vehiculo;

public class TallerService {

    private final ClienteDAO clienteDAO;
    private final VehiculoDAO vehiculoDAO;
    private final TrabajadorDAO trabajadorDAO;
    private final OrdenDAO ordenDAO;

    public TallerService(ClienteDAO clienteDAO, VehiculoDAO vehiculoDAO,
                         TrabajadorDAO trabajadorDAO, OrdenDAO ordenDAO) {
        if (clienteDAO == null || vehiculoDAO == null || trabajadorDAO == null || ordenDAO == null) {
            throw new IllegalArgumentException("Los DAO no pueden ser nulos.");
        }
        this.clienteDAO = clienteDAO;
        this.vehiculoDAO = vehiculoDAO;
        this.trabajadorDAO = trabajadorDAO;
        this.ordenDAO = ordenDAO;
    }

    //registro de clientes y vehiculos

    public void registrarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        if (clienteDAO.existe(cliente.getDocumento())) {
            throw new EntidadDuplicadaException("cliente", cliente.getDocumento());
        }
        clienteDAO.guardar(cliente);
    }

    public void registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo.");
        }
        String dueno = vehiculo.getCliente().getDocumento();
        if (!clienteDAO.existe(dueno)) {
            throw new EntidadNoEncontradaException("cliente", dueno);
        }
        if (vehiculoDAO.existe(vehiculo.getPlaca())) {
            throw new EntidadDuplicadaException("vehículo", vehiculo.getPlaca());
        }
        vehiculoDAO.guardar(vehiculo);
    }

    //gestión de órdenes de trabajo

    public void crearOrden(OrdenTrabajo orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden de trabajo no puede ser nula.");
        }
        if (ordenDAO.existe(orden.getId())) {
            throw new EntidadDuplicadaException("orden", orden.getId());
        }
        Vehiculo vehiculo = vehiculoDAO.buscarPorId(orden.getVehiculo().getPlaca())
                .orElseThrow(() -> new EntidadNoEncontradaException("vehículo", orden.getVehiculo().getPlaca()));
        if (!vehiculo.getCliente().getDocumento().equals(orden.getCliente().getDocumento())) {
            throw new IllegalArgumentException("El vehículo " + vehiculo.getPlaca() + " no pertenece al cliente de la orden.");
        }
        if (!trabajadorDAO.existe(orden.getTrabajador().getDocumento())) {
            throw new EntidadNoEncontradaException("trabajador", orden.getTrabajador().getDocumento());
        }
        ordenDAO.guardar(orden);
    }

    public List<OrdenTrabajo> listarOrdenes() {
        return ordenDAO.listarTodos();
    }

    public List<OrdenTrabajo> listarOrdenesPendientes() {
        return ordenDAO.listarPorEstado(EstadoOrden.PENDIENTE);
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.listarTodos();
    }

    public List<Vehiculo> listarVehiculos() {
        return vehiculoDAO.listarTodos();
    }

    public List<Trabajador> listarTrabajadores() {
        return trabajadorDAO.listarTodos();
    }

    // si otro hilo cambio la orden antes, lanza ConcurrenciaException
    public void cambiarEstadoOrden(String idOrden, EstadoOrden nuevoEstado) {
        if (idOrden == null || idOrden.isBlank()) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        }
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo.");
        }

        String id = idOrden.trim();
        OrdenTrabajo orden = ordenDAO.buscarPorId(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("orden", id));

        EstadoOrden actual = orden.getEstado();
        if (!actual.puedeCambiarA(nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(actual, nuevoEstado);
        }
        if (!ordenDAO.actualizarEstado(id, actual, nuevoEstado)) {
            throw new ConcurrenciaException(id);
        }
    }

    //MÉTODO ACORDADO CON T10 (Procesar Diagnóstico)

    public void procesarDiagnostico(String idOrden, EstadoOrden nuevoEstado) {
        cambiarEstadoOrden(idOrden, nuevoEstado);
    }
}
