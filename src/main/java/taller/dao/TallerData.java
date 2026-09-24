package taller.dao;

import taller.model.Cliente;
import taller.model.OrdenTrabajo;
import taller.model.Vehiculo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TallerData implements Serializable {

    private static final long serialVersionUID = 1L;

    private ArrayList<Cliente> clientes;
    private ArrayList<Vehiculo> vehiculos;
    private ArrayList<OrdenTrabajo> ordenes;

    public TallerData() {
        clientes = new ArrayList<>();
        vehiculos = new ArrayList<>();
        ordenes = new ArrayList<>();
    }

    public ArrayList<Cliente> getClientes() {
        return clientes;
    }

    public ArrayList<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public ArrayList<OrdenTrabajo> getOrdenes() {
        return ordenes;
    }

    public void agregarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }

        clientes.add(cliente);
    }

    public void agregarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo.");
        }

        vehiculos.add(vehiculo);
    }

    public void agregarOrden(OrdenTrabajo orden) {
        if (orden == null) {
            throw new IllegalArgumentException("La orden no puede ser nula.");
        }

        ordenes.add(orden);
    }

    public List<Cliente> consultarClientes() {
        return new ArrayList<>(clientes);
    }

    public List<Vehiculo> consultarVehiculos() {
        return new ArrayList<>(vehiculos);
    }

    public List<OrdenTrabajo> consultarOrdenes() {
        return new ArrayList<>(ordenes);
    }
}