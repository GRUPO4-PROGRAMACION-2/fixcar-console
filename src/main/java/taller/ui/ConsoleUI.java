package taller.ui;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import taller.controller.TallerController;
import taller.model.EstadoOrden;

/**
 * Consola del taller (T10).
 *
 * Responsabilidades de esta clase:
 *  - Mostrar el menú y leer datos con Scanner.
 *  - Validar solo el FORMATO de lo que escribe el usuario (que no esté vacío,
 *    que sea un número, que la opción exista).
 *  - Llamar a TallerController y mostrar el resultado o el error.
 *
 */
public class ConsoleUI {

    private final TallerController controller;
    private final Scanner scanner;

    public ConsoleUI(TallerController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
    }

    /** Ciclo principal: se repite hasta que el usuario elige 0 (Salir). */
    public void iniciar() {
        System.out.println("=== FixCar - Sistema del taller ===");

        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            try {
                int opcion = leerEntero("Elija una opción: ", 0, 6);
                continuar = ejecutarOpcion(opcion);
            } catch (NoSuchElementException e) {
                // La entrada se cerró (por ejemplo, Ctrl+Z y Enter en Windows).
                // En lugar de que el programa se caiga, salimos normalmente.
                System.out.println();
                continuar = false;
            }
        }

        System.out.println("Saliendo del sistema. ¡Hasta luego!");
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("---------------- Menú Principal ----------------");
        System.out.println("1. Registrar cliente");
        System.out.println("2. Registrar vehículo");
        System.out.println("3. Crear orden de trabajo");
        System.out.println("4. Listar órdenes");
        System.out.println("5. Cambiar estado de una orden");
        System.out.println("6. Enviar orden a diagnóstico");
        System.out.println("0. Salir");
        System.out.println("--------------------------------------");
    }

    /**
     * Ejecuta la opción elegida.
     * Devuelve false cuando el usuario quiere salir, true para seguir en el menú.
     */
    private boolean ejecutarOpcion(int opcion) {
        if (opcion == 0) {
            return false;
        }

        try {
            switch (opcion) {
                case 1 -> registrarCliente();
                case 2 -> registrarVehiculo();
                case 3 -> crearOrden();
                case 4 -> listarOrdenes();
                case 5 -> cambiarEstado();
                case 6 -> enviarADiagnostico();
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Errores esperados: datos que el servicio rechazó.
            System.out.println("No se pudo completar la operación: " + e.getMessage());
        } catch (RuntimeException e) {
            // Cualquier otro error: lo mostramos, pero el menú sigue funcionando.
            System.out.println("Ocurrió un error inesperado: " + e.getMessage());
        }
        return true;
    }

    // ===================== Opciones del menú =====================

    private void registrarCliente() {
        System.out.println("\n-- Registrar cliente --");
        String documento = leerTexto("Documento (DUI): ");
        String nombre = leerTexto("Nombre completo: ");
        String telefono = leerTexto("Teléfono: ");

        controller.registrarCliente(documento, nombre, telefono);
        System.out.println("Cliente registrado correctamente.");
    }

    private void registrarVehiculo() {
        System.out.println("\n-- Registrar vehículo --");
        String documentoCliente = leerTexto("Documento (DUI) del dueño: ");
        String placa = leerTexto("Placa: ").toUpperCase();
        String marca = leerTexto("Marca: ");
        String modelo = leerTexto("Modelo: ");

        controller.registrarVehiculo(placa, marca, modelo, documentoCliente);
        System.out.println("Vehículo " + placa + " registrado correctamente.");
    }

    private void crearOrden() {
        System.out.println("\n-- Crear orden de trabajo --");

        List<String> trabajadores = controller.listarTrabajadores();
        if (trabajadores.isEmpty()) {
            System.out.println("No hay trabajadores registrados. No se puede crear la orden.");
            return;
        }
        System.out.println("Trabajadores disponibles:");
        for (String trabajador : trabajadores) {
            System.out.println("  " + trabajador);
        }

        String placa = leerTexto("Placa del vehículo: ").toUpperCase();
        String codigoTrabajador = leerTexto("Código del trabajador: ");

        System.out.println("Tipo de servicio:");
        System.out.println("  1. Mantenimiento");
        System.out.println("  2. Reparación");
        int tipo = leerEntero("Elija el tipo: ", 1, 2);
        String tipoServicio = (tipo == 1) ? "MANTENIMIENTO" : "REPARACION";

        String descripcion = leerTexto("Descripción del problema: ");

        int numeroOrden = controller.crearOrden(placa, codigoTrabajador, tipoServicio, descripcion);
        System.out.println("Orden creada con el número " + numeroOrden + ".");
    }

    private void listarOrdenes() {
        System.out.println("\n-- Órdenes de trabajo --");
        List<String> ordenes = controller.listarOrdenes();

        if (ordenes.isEmpty()) {
            System.out.println("No hay órdenes registradas.");
            return;
        }
        for (String orden : ordenes) {
            System.out.println(orden);
        }
        System.out.println("Total: " + ordenes.size() + " orden(es).");
    }

    private void cambiarEstado() {
        System.out.println("\n-- Cambiar estado de una orden --");
        int numeroOrden = leerNumeroOrden();

        // Se muestran los estados que existan en EstadoOrden (T05).
        // Si T05 agrega o quita estados, este menú se ajusta solo.
        EstadoOrden[] estados = EstadoOrden.values();
        System.out.println("Nuevo estado:");
        for (int i = 0; i < estados.length; i++) {
            System.out.println("  " + (i + 1) + ". " + estados[i]);
        }
        int eleccion = leerEntero("Elija el estado: ", 1, estados.length);
        EstadoOrden nuevoEstado = estados[eleccion - 1];

        // Si el cambio no está permitido, el servicio lanza una excepción
        // y ejecutarOpcion() muestra el mensaje.
        controller.cambiarEstado(numeroOrden, nuevoEstado);
        System.out.println("La orden " + numeroOrden + " ahora está en " + nuevoEstado + ".");
    }

    private void enviarADiagnostico() {
        System.out.println("\n-- Enviar orden a diagnóstico --");
        int numeroOrden = leerNumeroOrden();

        // El diagnóstico se procesa en segundo plano (T08),
        // por eso este método regresa enseguida y el menú no se bloquea.
        controller.enviarADiagnostico(numeroOrden);
        System.out.println("Orden " + numeroOrden + " enviada a diagnóstico.");
        System.out.println("Puede seguir usando el menú; revise el avance con la opción 4.");
    }

    // ===================== Lectura de datos =====================

    /** Pide un texto y repite la pregunta hasta que no esté vacío. */
    private String leerTexto(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("Este dato es obligatorio.");
        }
    }

    /** Pide un número entero entre min y max (incluidos) y repite si no es válido. */
    private int leerEntero(String mensaje, int min, int max) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                int numero = Integer.parseInt(texto);
                if (numero >= min && numero <= max) {
                    return numero;
                }
            } catch (NumberFormatException e) {
                // No era un número: se muestra el aviso de abajo y se vuelve a pedir.
            }
            System.out.println("Ingrese un número entre " + min + " y " + max + ".");
        }
    }

    /** Pide un número de orden (entero positivo). */
    private int leerNumeroOrden() {
        while (true) {
            String texto = leerTexto("Número de orden: ");
            try {
                int numero = Integer.parseInt(texto);
                if (numero > 0) {
                    return numero;
                }
            } catch (NumberFormatException e) {
                // No era un número: se vuelve a pedir.
            }
            System.out.println("El número de orden debe ser un entero positivo.");
        }
    }
}
