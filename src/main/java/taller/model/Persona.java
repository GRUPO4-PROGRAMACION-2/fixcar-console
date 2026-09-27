package taller.model;

import java.io.Serializable;

public abstract class Persona implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private String documento;
    private String telefono;

    public Persona(String nombre, String documento, String telefono) {
        setNombre(nombre);
        setDocumento(documento);
        setTelefono(telefono);
    }

    // Cada tipo de persona dice cuál es
    public abstract String getTipo();

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento no puede estar vacío");
        }
        this.documento = documento.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono no puede estar vacío");
        }
        this.telefono = telefono.trim();
    }

    @Override
    public String toString() {
        return getTipo() + ": " + nombre + " (doc: " + documento + ", tel: " + telefono + ")";
    }
}