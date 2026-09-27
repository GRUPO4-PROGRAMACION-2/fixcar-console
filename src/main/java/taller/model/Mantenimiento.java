package taller.model;

/**
 * Servicio de mantenimiento preventivo del vehículo.
 */
public class Mantenimiento extends Servicio {

    private static final long serialVersionUID = 1L;

    public Mantenimiento(String nombre, double precioBase) {
        super(nombre, precioBase);
    }

    @Override
    public double calcularCosto() {
        return getPrecioBase();
    }
}
