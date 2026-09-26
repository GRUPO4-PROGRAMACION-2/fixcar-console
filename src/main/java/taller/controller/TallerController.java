package taller.controller;

import java.util.List;

import taller.model.Cliente;
import taller.model.EstadoOrden;
import taller.model.OrdenTrabajo;
import taller.model.Vehiculo;
import taller.service.TallerService;


    //Conección entre menú y reglas:Las reglas basicas del taller 
    // se encuentran en TallerService.
 
public class TallerController {

    private final TallerService tallerService;

    public TallerController(TallerService tallerService) {
        if (tallerService == null) {
            throw new IllegalArgumentException("El servicio del taller no puede ser nulo.");
        }
        this.tallerService = tallerService;
    }

    public void registrarCliente(Cliente cliente) {
        tallerService.registrarCliente(cliente);
    }

    public void registrarVehiculo(Vehiculo vehiculo) {
        tallerService.registrarVehiculo(vehiculo);
    }

    public void crearOrden(OrdenTrabajo orden) {
        tallerService.crearOrden(orden);
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

    public void cambiarEstadoOrden(String idOrden, EstadoOrden nuevoEstado) {
        tallerService.cambiarEstadoOrden(idOrden, nuevoEstado);
    }

    //Procesa el diagnóstico de una orden mediante las reglas del servicio.
    public void procesarDiagnostico(String idOrden, EstadoOrden nuevoEstado) {
        tallerService.procesarDiagnostico(idOrden, nuevoEstado);
    }
}
