package taller.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrdenTrabajo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private Cliente cliente;
    private Vehiculo vehiculo;
    private Trabajador trabajador;
    private List<Servicio> servicios;
    private EstadoOrden estado;
    private LocalDateTime fechaCreacion;

    public OrdenTrabajo(String id, Cliente cliente, Vehiculo vehiculo, Trabajador trabajador) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la orden no puede estar vacío");
        }
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo");
        }
        if (trabajador == null) {
            throw new IllegalArgumentException("El trabajador no puede ser nulo");
        }
        this.id = id.trim();
        this.cliente = cliente;
        this.vehiculo = vehiculo;
        this.trabajador = trabajador;
        this.servicios = new ArrayList<>();
        this.estado = EstadoOrden.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    public void agregarServicio(Servicio servicio) {
        if (servicio == null) {
            throw new IllegalArgumentException("El servicio no puede ser nulo");
        }
        servicios.add(servicio);
    }

    public double calcularCostoTotal() {
        double total = 0;
        for (Servicio servicio : servicios) {
            total += servicio.calcularCosto();
        }
        return total;
    }

    public void cambiarEstado(EstadoOrden nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
        this.estado = nuevoEstado;
    }

    public String getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Trabajador getTrabajador() {
        return trabajador;
    }

    public List<Servicio> getServicios() {
        return Collections.unmodifiableList(servicios);
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    @Override
    public String toString() {
        return "Orden " + id + " [" + estado + "] - " + vehiculo.getPlaca()
                + " - cliente: " + cliente.getNombre()
                + " - total: " + calcularCostoTotal();
    }
}
