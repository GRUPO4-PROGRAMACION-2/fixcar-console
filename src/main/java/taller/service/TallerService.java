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
        tallerDAO.actualizarDatos(datos -> datos.agregarCliente(cliente));
    }

    public void registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo.");
        }
        tallerDAO.actualizarDatos(datos -> datos.agregarVehiculo(vehiculo));
    }

    //gestión de órdenes de trabajo

    public void crearOrden(OrdenTrabajo orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden de trabajo no puede ser nula.");
        }
        tallerDAO.actualizarDatos(datos -> datos.agregarOrden(orden));
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

        String idNormalizado = idOrden.trim();
        tallerDAO.actualizarDatos(datos -> {
            OrdenTrabajo ordenEncontrada = datos.getOrdenes().stream()
                    .filter(orden -> orden.getId().equals(idNormalizado))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró la orden con ID: " + idOrden));

            ordenEncontrada.cambiarEstado(nuevoEstado);
        });
    }

    //MÉTODO ACORDADO CON T10 (Procesar Diagnóstico) 

    public void procesarDiagnostico(String idOrden, EstadoOrden nuevoEstado) {
        // Método expuesto para que T10 o el Controller procese el diagnóstico de la orden
        cambiarEstadoOrden(idOrden, nuevoEstado);
    }
}
