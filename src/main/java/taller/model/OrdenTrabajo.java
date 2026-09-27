package taller.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrdenTrabajo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String descripcion;
    private Cliente cliente;
    private Vehiculo vehiculo;
    private Trabajador trabajador;
    // Se declara como ArrayList (y no como List) porque es el tipo que
    // Java necesita reconstruir al leer la orden de taller.dat.
    private ArrayList<Servicio> servicios;
    private EstadoOrden estado;
    private LocalDateTime fechaCreacion;
    private String diagnostico;
    private LocalDateTime fechaDiagnostico;

    public OrdenTrabajo(String id, String descripcion, Cliente cliente, Vehiculo vehiculo, Trabajador trabajador) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la orden no puede estar vacío");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción de la orden no puede estar vacía");
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
        this.descripcion = descripcion.trim();
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
        if (!estado.puedeCambiarA(nuevoEstado)) {
            throw new IllegalStateException("No se permite cambiar el estado de " + estado + " a " + nuevoEstado);
        }
        this.estado = nuevoEstado;
    }

    /**
     * Guarda el resultado del diagnóstico técnico de la orden y la deja
     * lista para la reparación (EN_PROCESO).
     *
     * El estado no se cambia con un setter público: el propio modelo decide
     * que solo una orden pendiente puede pasar por diagnóstico, y el texto
     * nunca puede llegar vacío.
     */
    public void registrarDiagnostico(String textoDiagnostico) {
        if (textoDiagnostico == null || textoDiagnostico.isBlank()) {
            throw new IllegalArgumentException("El diagnóstico no puede estar vacío");
        }
        if (estado != EstadoOrden.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo una orden PENDIENTE puede registrar su diagnóstico; la orden está en " + estado);
        }

        this.diagnostico = textoDiagnostico.trim();
        this.fechaDiagnostico = LocalDateTime.now();
        cambiarEstado(EstadoOrden.EN_PROCESO);
    }

    public String getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
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

    /** Texto del diagnóstico técnico, o null si la orden todavía no se ha diagnosticado. */
    public String getDiagnostico() {
        return diagnostico;
    }

    public LocalDateTime getFechaDiagnostico() {
        return fechaDiagnostico;
    }

    public boolean fueDiagnosticada() {
        return diagnostico != null;
    }

    @Override
    public String toString() {
        return "Orden " + id + " [" + estado + "] - " + vehiculo.getPlaca()
                + " - cliente: " + cliente.getNombre()
                + " - total: " + calcularCostoTotal();
    }
}
