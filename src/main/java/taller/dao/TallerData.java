package taller.dao;

import taller.model.Cliente;
import taller.model.OrdenTrabajo;
import taller.model.Vehiculo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Contenedor de datos del taller (T06).
 *
 * Se serializa completo dentro de taller.dat, por eso guarda directamente las
 * listas que Java necesita poder reconstruir al leer el archivo. Las listas
 * internas nunca se entregan tal cual: las consultas devuelven copias de solo
 * lectura para que nadie modifique los datos por fuera de la capa de servicio.
 */
public class TallerData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ArrayList<Cliente> clientes;
    private final ArrayList<Vehiculo> vehiculos;
    private final ArrayList<OrdenTrabajo> ordenes;

    public TallerData() {
        clientes = new ArrayList<>();
        vehiculos = new ArrayList<>();
        ordenes = new ArrayList<>();
    }

    // ========== Altas (solo las usa TallerService a través del DAO) ==========

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

    // ========== Consultas: copias inmutables de la información ==========

    public List<Cliente> consultarClientes() {
        return Collections.unmodifiableList(new ArrayList<>(clientes));
    }

    public List<Vehiculo> consultarVehiculos() {
        return Collections.unmodifiableList(new ArrayList<>(vehiculos));
    }

    public List<OrdenTrabajo> consultarOrdenes() {
        return Collections.unmodifiableList(new ArrayList<>(ordenes));
    }

    // ========== Búsquedas por clave natural ==========

    /** Busca un cliente por su documento, sin distinguir mayúsculas. */
    public Optional<Cliente> buscarClientePorDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            return Optional.empty();
        }

        String clave = documento.trim();
        return clientes.stream()
                .filter(cliente -> cliente.getDocumento().equalsIgnoreCase(clave))
                .findFirst();
    }

    /** Busca un vehículo por su placa, sin distinguir mayúsculas. */
    public Optional<Vehiculo> buscarVehiculoPorPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            return Optional.empty();
        }

        String clave = placa.trim();
        return vehiculos.stream()
                .filter(vehiculo -> vehiculo.getPlaca().equalsIgnoreCase(clave))
                .findFirst();
    }

    /** Busca una orden de trabajo por su identificador. */
    public Optional<OrdenTrabajo> buscarOrden(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        String clave = id.trim();
        return ordenes.stream()
                .filter(orden -> orden.getId().equalsIgnoreCase(clave))
                .findFirst();
    }
}
