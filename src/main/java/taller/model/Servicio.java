package taller.model;

import java.io.Serializable;

/**
 * Base común para los servicios que puede realizar el taller.
 */
public abstract class Servicio implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final double precioBase;

    protected Servicio(String nombre, double precioBase) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del servicio no puede estar vacío");
        }
        if (precioBase < 0 || !Double.isFinite(precioBase)) {
            throw new IllegalArgumentException("El precio base debe ser un número positivo");
        }

        this.nombre = nombre.trim();
        this.precioBase = precioBase;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    /**
     * Calcula el costo final según el tipo de servicio.
     */
    public abstract double calcularCosto();

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + nombre
                + " (costo: " + calcularCosto() + ")";
    }
}
