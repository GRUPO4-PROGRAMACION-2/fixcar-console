package taller.model;

/**
 * Servicio de reparación correctiva del vehículo.
 */
public class Reparacion extends Servicio {

    private static final long serialVersionUID = 1L;

    private final double costoManoObra;

    public Reparacion(String nombre, double precioBase, double costoManoObra) {
        super(nombre, precioBase);
        if (costoManoObra < 0 || !Double.isFinite(costoManoObra)) {
            throw new IllegalArgumentException("El costo de mano de obra debe ser positivo");
        }
        this.costoManoObra = costoManoObra;
    }

    public double getCostoManoObra() {
        return costoManoObra;
    }

    @Override
    public double calcularCosto() {
        return getPrecioBase() + getCostoManoObra();
    }
}
