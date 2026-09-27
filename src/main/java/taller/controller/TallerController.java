package taller.controller;

import taller.model.Cliente;
import taller.model.EstadoOrden;
import taller.model.OrdenTrabajo;
import taller.model.Servicio;
import taller.model.Trabajador;
import taller.model.Vehiculo;
import taller.service.ProcesadorOrdenes;
import taller.service.TallerService;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/**
 * Puente entre la consola y el taller (T09).
 *
 * Recibe los datos que escribió el usuario, los traduce a objetos del modelo
 * y los entrega al servicio. También es quien manda a diagnóstico las órdenes
 * al procesador de segundo plano. No repite reglas de negocio: las valida
 * TallerService y las aplica OrdenTrabajo.
 */
public class TallerController implements AutoCloseable {

    private final TallerService tallerService;
    private final ProcesadorOrdenes procesadorOrdenes;

    public TallerController(TallerService tallerService, ProcesadorOrdenes procesadorOrdenes) {
        if (tallerService == null) {
            throw new IllegalArgumentException("El servicio de taller no puede ser nulo");
        }

        if (procesadorOrdenes == null) {
            throw new IllegalArgumentException("El procesador de órdenes no puede ser nulo.");
        }

        this.tallerService = tallerService;
        this.procesadorOrdenes = procesadorOrdenes;
    }

    // ===================== Clientes y vehículos =====================

    public Cliente registrarCliente(String nombre, String documento, String telefono,
                                    String correo, String direccion) {
        Cliente cliente = new Cliente(nombre, documento, telefono, correo, direccion);
        tallerService.registrarCliente(cliente);
        return cliente;
    }

    public Vehiculo registrarVehiculo(String placa, String marca, String modelo, Cliente cliente) {
        Vehiculo vehiculo = new Vehiculo(placa, marca, modelo, cliente);
        tallerService.registrarVehiculo(vehiculo);
        return vehiculo;
    }

    public Optional<Cliente> buscarClientePorDocumento(String documento) {
        return tallerService.buscarClientePorDocumento(documento);
    }

    public Optional<Vehiculo> buscarVehiculoPorPlaca(String placa) {
        return tallerService.buscarVehiculoPorPlaca(placa);
    }

    // ===================== Órdenes de trabajo =====================

    public OrdenTrabajo crearOrden(String descripcion, Cliente cliente, Vehiculo vehiculo,
                                   Trabajador trabajador, List<Servicio> servicios) {
        return tallerService.crearOrden(descripcion, cliente, vehiculo, trabajador, servicios);
    }

    public List<Cliente> listarClientes() {
        return tallerService.listarClientes();
    }

    public List<Vehiculo> listarVehiculos() {
        return tallerService.listarVehiculos();
    }

    public List<OrdenTrabajo> listarOrdenes() {
        return tallerService.listarOrdenes();
    }

    public Optional<OrdenTrabajo> buscarOrden(String id) {
        return tallerService.buscarOrden(id);
    }

    public void cambiarEstadoOrden(String idOrden, EstadoOrden nuevoEstado) {
        tallerService.cambiarEstadoOrden(idOrden, nuevoEstado);
    }

    /**
     * Manda una orden al diagnóstico en segundo plano. Antes de encolar se
     * revisa que la orden exista y esté PENDIENTE, para avisar de inmediato
     * en la consola en lugar de fallar más tarde en segundo plano.
     */
    public Future<?> encolarDiagnostico(String idOrden) {
        if (idOrden == null || idOrden.isBlank()) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        }

        String id = idOrden.trim();
        OrdenTrabajo orden = tallerService.buscarOrden(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe la orden " + id));

        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new IllegalStateException(
                    "La orden " + id + " solo se puede enviar a diagnóstico cuando está PENDIENTE; está en "
                            + orden.getEstado());
        }

        try {
            return procesadorOrdenes.encolarDiagnostico(id);
        } catch (RejectedExecutionException e) {
            throw new IllegalStateException(
                    "El procesador de órdenes está cerrado; no se pueden recibir más diagnósticos.");
        }
    }

    @Override
    public void close() {
        procesadorOrdenes.close();
    }
}
