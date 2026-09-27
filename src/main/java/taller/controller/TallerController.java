package taller.controller;

import taller.model.*;
import taller.service.ProcesadorOrdenes;
import taller.service.TallerService;

import java.util.List;
import java.util.concurrent.Future;

public class TallerController implements AutoCloseable{
    private final TallerService tallerService;
    private final ProcesadorOrdenes procesadorOrdenes;

    public TallerController(TallerService tallerService, ProcesadorOrdenes procesadorOrdenes){
        if (tallerService == null) {
            throw new IllegalArgumentException("El servicio de taller no puede ser nulo");
        }

        if (procesadorOrdenes == null) {
            throw new IllegalArgumentException("El procesador de órdenes no puede ser nulo.");
        }

        this.tallerService = tallerService;
        this.procesadorOrdenes = procesadorOrdenes;
    }

    public Cliente registrarCliente(String nombre, String documento, String telefono, String correo, String direccion){
        Cliente cliente = new Cliente(nombre, documento, telefono, correo, direccion);
        tallerService.registrarCliente(cliente);
        return cliente;
    }

    public Vehiculo registrarVehiculo(String placa, String marca, String modelo, Cliente cliente){
        Vehiculo vehiculo = new Vehiculo(placa, marca, modelo, cliente);
        tallerService.registrarVehiculo(vehiculo);
        return vehiculo;
    }

    public OrdenTrabajo crearOrden(String id, String descripcion, Cliente cliente, Vehiculo vehiculo, Trabajador trabajador, List<Servicio> servicios){
        OrdenTrabajo orden = new OrdenTrabajo(id, descripcion, cliente, vehiculo, trabajador);

        if(servicios != null){
            for (Servicio servicio : servicios){
                orden.agregarServicio(servicio);
            }
        }

        tallerService.crearOrden(orden);
        return orden;
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

    public void cambiarEstadoOrden(String idOrden, EstadoOrden nuevoEstado){
        tallerService.cambiarEstadoOrden(idOrden, nuevoEstado);
    }

    public Future<?> encolarDiagnostico(String idOrden, EstadoOrden nuevoEstado){
        return procesadorOrdenes.encolarDiagnostico(idOrden, nuevoEstado);
    }

    @Override
    public void close(){
        procesadorOrdenes.close();
    }

}
