package taller.model;

public class Trabajador extends Persona {

    private static final long serialVersionUID = 1L;

    private String cargo;          // Ej: "Mecánico", "Recepcionista"
    private String especialidad;   // Ej: "Frenos", "Electricidad automotriz"

    public Trabajador(String nombre, String documento, String telefono,
                      String cargo, String especialidad) {
        super(nombre, documento, telefono);
        setCargo(cargo);
        setEspecialidad(especialidad);
    }

    @Override
    public String getTipo() {
        return "Trabajador";
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        if (cargo == null || cargo.isBlank()) {
            throw new IllegalArgumentException("El cargo no puede estar vacío");
        }
        this.cargo = cargo.trim();
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
}