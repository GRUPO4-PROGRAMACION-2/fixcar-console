package taller.service;

import java.util.List;

import taller.dao.TallerDAO;
import taller.dao.TallerData;
import taller.model.Cliente;
import taller.model.EstadoOrden;
import taller.model.OrdenTrabajo;
import taller.model.Vehiculo;

public class TallerService {

    private final TallerDAO tallerDAO;

    public TallerService(TallerDAO tallerDAO) {
        if (tallerDAO == null) {
            throw new IllegalArgumentException("El DAO no puede ser nulo.");
        }
        this.tallerDAO = tallerDAO;
    }

    //registro de clientes y vehuculos

    public void registrarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        TallerData datos = tallerDAO.cargar();
        datos.agregarCliente(cliente);
        tallerDAO.guardar(datos);
    }

    public void registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo.");
        }
        TallerData datos = tallerDAO.cargar();
        datos.agregarVehiculo(vehiculo);
        tallerDAO.guardar(datos);
    }

    //gestión de órdenes de trabajo

    public void crearOrden(OrdenTrabajo orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden de trabajo no puede ser nula.");
        }
        TallerData datos = tallerDAO.cargar();
        datos.agregarOrden(orden);
        tallerDAO.guardar(datos);
    }

    public List<OrdenTrabajo> listarOrdenes() {
        TallerData datos = tallerDAO.cargar();
        return datos.consultarOrdenes();
    }

    public List<Cliente> listarClientes() {
        TallerData datos = tallerDAO.cargar();
        return datos.consultarClientes();
    }

    public List<Vehiculo> listarVehiculos() {
        TallerData datos = tallerDAO.cargar();
        return datos.consultarVehiculos();
    }

    public void cambiarEstadoOrden(String idOrden, EstadoOrden nuevoEstado) {
        if (idOrden == null || idOrden.isBlank()) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        }
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo.");
        }

        TallerData datos = tallerDAO.cargar();
        List<OrdenTrabajo> ordenes = datos.getOrdenes();

        OrdenTrabajo ordenEncontrada = ordenes.stream()
                .filter(o -> o.getId().equals(idOrden.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la orden con ID: " + idOrden));

        //aplica la validación interna de la orden
        ordenEncontrada.cambiarEstado(nuevoEstado);

        //Persiste el cambio
        tallerDAO.guardar(datos);
    }

    //MÉTODO ACORDADO CON T10 (Procesar Diagnóstico) 

    public void procesarDiagnostico(String idOrden, EstadoOrden nuevoEstado) {
        // Método expuesto para que T10 o el Controller procese el diagnóstico de la orden
        cambiarEstadoOrden(idOrden, nuevoEstado);
    }
}