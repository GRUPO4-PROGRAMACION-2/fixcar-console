package taller.model;

public class Cliente extends Persona {

    private static final long serialVersionUID = 1L;

    private String correo;
    private String direccion;

    public Cliente(String nombre, String documento, String telefono,
                   String correo, String direccion) {
        super(nombre, documento, telefono);
        setCorreo(correo);
        setDireccion(direccion);
    }

    @Override
    public String getTipo() {
        return "Cliente";
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
    if (correo == null) {
        throw new IllegalArgumentException("El correo no es válido");
    }
    String limpio = correo.trim();
    int arroba = limpio.indexOf('@');

    // Debe haber un solo '@', con texto antes y texto después
    boolean valido = arroba > 0
            && arroba == limpio.lastIndexOf('@')
            && arroba < limpio.length() - 1;

    if (!valido) {
        throw new IllegalArgumentException("El correo no es válido");
    }
    this.correo = limpio;
}

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}