package taller.soporte;

import taller.model.Cliente;
import taller.model.Mantenimiento;
import taller.model.OrdenTrabajo;
import taller.model.Reparacion;
import taller.model.Trabajador;
import taller.model.Vehiculo;

public final class Fixtures {
    private Fixtures() {
    }

    public static Cliente cliente(String documento) {
        return new Cliente("Ana Pérez", documento, "7000-1111", "ana@correo.com", "San Salvador");
    }

    public static Vehiculo vehiculo(Cliente dueno, String placa) {
        return new Vehiculo(placa, "Toyota", "Corolla", dueno);
    }

    public static Trabajador elisa() {
        return new Trabajador("Elisa", "T-001", "7000-0001", "Mecánica", "Motor");
    }

    public static OrdenTrabajo orden(String id, Cliente cliente, Vehiculo vehiculo) {
        OrdenTrabajo orden = new OrdenTrabajo(id, "Ruido en los frenos", cliente, vehiculo, elisa());
        orden.agregarServicio(new Mantenimiento("Cambio de aceite", 30));
        orden.agregarServicio(new Reparacion("Cambio de pastillas", 50, 10));
        return orden;
    }
}
