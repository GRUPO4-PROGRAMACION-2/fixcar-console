package taller.model;

import java.io.Serializable;

public class Vehiculo implements Serializable {
    private static final long serialVersionUID = 1L;

    // Atributos privados  
    private String placa;
    private String marca;
    private String modelo;
    private String cliente;

    public Vehiculo() {
    }

    // Constructor con parámetros
    public Vehiculo(String placa, String marca, String modelo, String cliente) {
        setPlaca(placa); // Usa el setter para aplicar la validación
        this.marca = marca;
        this.modelo = modelo;
        this.cliente = cliente;
    }

    // Getter y Setter para 'placa' con validación
    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            throw new IllegalArgumentException("La placa no puede estar vacía.");
        }
        this.placa = placa;
    }

    // Getters y Setters para los demás atributos
    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    @Override
    public String toString() {
        return "Vehiculo{" +
                "placa='" + placa + '\'' +
                ", marca='" + marca + '\'' +
                ", modelo='" + modelo + '\'' +
                ", cliente='" + cliente + '\'' +
                '}';
    }
}