package taller.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import taller.controller.TallerController;
import taller.model.Cliente;
import taller.model.EstadoOrden;
import taller.model.Mantenimiento;
import taller.model.OrdenTrabajo;
import taller.model.Reparacion;
import taller.model.Servicio;
import taller.model.Trabajador;
import taller.model.Vehiculo;

/**
 * Consola del taller (T10).
 *
 * Responsabilidades de esta clase:
 *  - Mostrar el menú y leer datos con Scanner.
 *  - Validar solo el FORMATO de lo que escribe el usuario (que no esté vacío,
 *    que sea un número, que la opción exista).
 *  - Buscar en las listas del controller los objetos que piden sus métodos
 *    (Cliente, Vehiculo, OrdenTrabajo) y mostrar los resultados.
 */
public class ConsoleUI {

    private final TallerController controller;
    private final Scanner scanner;

    // El sistema todavía no guarda trabajadores (TallerData no tiene una lista para ellos),
    // así que la consola usa esta plantilla fija. Pendiente de acordar con el equipo.
    private final List<Trabajador> trabajadores;

    public ConsoleUI(TallerController controller, Scanner scanner) {
        this.controller = controller;
        this.scanner = scanner;
        this.trabajadores = List.of(
                new Trabajador("Elisa", "T-001", "7000-0001", "Mecánica", "Motor"),
                new Trabajador("Dilan", "T-002", "7000-0002", "Mecánico", "Electricidad automotriz"));
    }

    /** Ciclo principal: se repite hasta que el usuario elige 0 (Salir). */
    public void iniciar() {
        System.out.println("=== FixCar - Sistema del taller ===");

        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            try {
                int opcion = leerEntero("Elija una opción: ", 0, 7);
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
        System.out.println("7. Listar clientes y vehículos");
        System.out.println("0. Salir");
        System.out.println("------------------------------------------------");
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
                case 7 -> listarClientesYVehiculos();
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Errores esperados: datos que el modelo o el servicio rechazaron.
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
        String nombre = leerTexto("Nombre completo: ");
        String documento = leerTexto("Documento (DUI): ");
        String telefono = leerTexto("Teléfono: ");
        String correo = leerTexto("Correo: ");
        String direccion = leerTexto("Dirección: ");

        Cliente cliente = controller.registrarCliente(nombre, documento, telefono, correo, direccion);
        System.out.println("Cliente " + cliente.getNombre() + " registrado correctamente.");
    }

    private void registrarVehiculo() {
        System.out.println("\n-- Registrar vehículo --");
        String documento = leerTexto("Documento (DUI) del dueño: ");

        Cliente cliente = buscarCliente(documento);
        if (cliente == null) {
            System.out.println("No existe un cliente con documento " + documento
                    + ". Regístrelo primero con la opción 1.");
            return;
        }
        System.out.println("Dueño: " + cliente.getNombre());

        String placa = leerTexto("Placa: ").toUpperCase();
        String marca = leerTexto("Marca: ");
        String modelo = leerTexto("Modelo: ");

        controller.registrarVehiculo(placa, marca, modelo, cliente);
        System.out.println("Vehículo " + placa + " registrado correctamente.");
    }

    private void crearOrden() {
        System.out.println("\n-- Crear orden de trabajo --");
        String placa = leerTexto("Placa del vehículo: ");

        Vehiculo vehiculo = buscarVehiculo(placa);
        if (vehiculo == null) {
            System.out.println("No existe un vehículo con placa " + placa.toUpperCase()
                    + ". Regístrelo primero con la opción 2.");
            return;
        }
        // El cliente de la orden es el dueño del vehículo.
        Cliente cliente = vehiculo.getCliente();
        System.out.println("Vehículo: " + vehiculo.getMarca() + " " + vehiculo.getModelo()
                + " - dueño: " + cliente.getNombre());

        Trabajador trabajador = elegirTrabajador();
        String descripcion = leerTexto("Descripción del problema: ");
        List<Servicio> servicios = leerServicios();

        String id = generarIdOrden();
        OrdenTrabajo orden = controller.crearOrden(id, descripcion, cliente, vehiculo, trabajador, servicios);
        System.out.println("Orden " + orden.getId() + " creada. Total: $"
                + formatearDinero(orden.calcularCostoTotal()));
    }

    private void listarOrdenes() {
        System.out.println("\n-- Órdenes de trabajo --");
        List<OrdenTrabajo> ordenes = controller.listarOrdenes();

        if (ordenes.isEmpty()) {
            System.out.println("No hay órdenes registradas.");
            return;
        }
        for (OrdenTrabajo orden : ordenes) {
            System.out.println(orden.getId() + " | " + orden.getEstado()
                    + " | " + orden.getVehiculo().getPlaca()
                    + " | Cliente: " + orden.getCliente().getNombre()
                    + " | Trabajador: " + orden.getTrabajador().getNombre()
                    + " | Total: $" + formatearDinero(orden.calcularCostoTotal()));
            for (Servicio servicio : orden.getServicios()) {
                System.out.println("      - " + servicio.getClass().getSimpleName() + ": "
                        + servicio.getNombre() + " ($" + formatearDinero(servicio.calcularCosto()) + ")");
            }
        }
        System.out.println("Total: " + ordenes.size() + " orden(es).");
    }

    private void cambiarEstado() {
        System.out.println("\n-- Cambiar estado de una orden --");
        OrdenTrabajo orden = pedirOrden();
        if (orden == null) {
            return;
        }

        EstadoOrden nuevoEstado = elegirSiguienteEstado(orden);
        if (nuevoEstado == null) {
            return;
        }

        controller.cambiarEstadoOrden(orden.getId(), nuevoEstado);
        System.out.println("La orden " + orden.getId() + " ahora está en " + nuevoEstado + ".");
    }

    private void enviarADiagnostico() {
        System.out.println("\n-- Enviar orden a diagnóstico --");
        OrdenTrabajo orden = pedirOrden();
        if (orden == null) {
            return;
        }

        EstadoOrden nuevoEstado = elegirSiguienteEstado(orden);
        if (nuevoEstado == null) {
            return;
        }

        // El diagnóstico se procesa en segundo plano (T08),
        // por eso este método regresa enseguida y el menú no se bloquea.
        controller.encolarDiagnostico(orden.getId(), nuevoEstado);
        System.out.println("Orden " + orden.getId() + " enviada a diagnóstico (destino: " + nuevoEstado + ").");
        System.out.println("Puede seguir usando el menú; revise el avance con la opción 4.");
    }

    private void listarClientesYVehiculos() {
        System.out.println("\n-- Clientes --");
        List<Cliente> clientes = controller.listarClientes();
        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
        }
        for (Cliente cliente : clientes) {
            System.out.println(cliente.getDocumento() + " | " + cliente.getNombre()
                    + " | Tel: " + cliente.getTelefono() + " | " + cliente.getCorreo());
        }

        System.out.println("\n-- Vehículos --");
        List<Vehiculo> vehiculos = controller.listarVehiculos();
        if (vehiculos.isEmpty()) {
            System.out.println("No hay vehículos registrados.");
        }
        for (Vehiculo vehiculo : vehiculos) {
            System.out.println(vehiculo.getPlaca() + " | " + vehiculo.getMarca() + " " + vehiculo.getModelo()
                    + " | Dueño: " + vehiculo.getCliente().getNombre());
        }
    }

    // ===================== Búsquedas en las listas del controller =====================

    /** Devuelve el cliente con ese documento, o null si no existe. */
    private Cliente buscarCliente(String documento) {
        for (Cliente cliente : controller.listarClientes()) {
            if (cliente.getDocumento().equalsIgnoreCase(documento.trim())) {
                return cliente;
            }
        }
        return null;
    }

    /** Devuelve el vehículo con esa placa, o null si no existe. */
    private Vehiculo buscarVehiculo(String placa) {
        for (Vehiculo vehiculo : controller.listarVehiculos()) {
            if (vehiculo.getPlaca().trim().equalsIgnoreCase(placa.trim())) {
                return vehiculo;
            }
        }
        return null;
    }

    /** Devuelve la orden con ese ID, o null si no existe. */
    private OrdenTrabajo buscarOrden(String id) {
        for (OrdenTrabajo orden : controller.listarOrdenes()) {
            if (orden.getId().equalsIgnoreCase(id.trim())) {
                return orden;
            }
        }
        return null;
    }

    /** Pide el ID de una orden; si no existe, avisa y devuelve null. */
    private OrdenTrabajo pedirOrden() {
        String id = leerTexto("ID de la orden (ej. O-1): ").toUpperCase();
        OrdenTrabajo orden = buscarOrden(id);
        if (orden == null) {
            System.out.println("No existe la orden " + id + ".");
        }
        return orden;
    }

    /**
     * Genera el siguiente ID de orden (O-1, O-2, ...) a partir del número
     * más alto que ya existe, para que no se repitan aunque se reinicie el programa.
     */
    private String generarIdOrden() {
        int mayor = 0;
        for (OrdenTrabajo orden : controller.listarOrdenes()) {
            String id = orden.getId();
            if (id.startsWith("O-")) {
                try {
                    int numero = Integer.parseInt(id.substring(2));
                    if (numero > mayor) {
                        mayor = numero;
                    }
                } catch (NumberFormatException e) {
                    // ID con otro formato: se ignora.
                }
            }
        }
        return "O-" + (mayor + 1);
    }

    // ===================== Selecciones =====================

    private Trabajador elegirTrabajador() {
        System.out.println("Trabajadores disponibles:");
        for (int i = 0; i < trabajadores.size(); i++) {
            Trabajador t = trabajadores.get(i);
            System.out.println("  " + (i + 1) + ". " + t.getNombre()
                    + " (" + t.getCargo() + ", " + t.getEspecialidad() + ")");
        }
        int eleccion = leerEntero("Elija el trabajador: ", 1, trabajadores.size());
        return trabajadores.get(eleccion - 1);
    }

    /**
     * Muestra solo los estados a los que la orden puede pasar,
     * usando la regla que ya define EstadoOrden (puedeCambiarA).
     * Devuelve null si la orden ya no puede cambiar de estado.
     */
    private EstadoOrden elegirSiguienteEstado(OrdenTrabajo orden) {
        EstadoOrden actual = orden.getEstado();

        List<EstadoOrden> opciones = new ArrayList<>();
        for (EstadoOrden estado : EstadoOrden.values()) {
            if (actual.puedeCambiarA(estado)) {
                opciones.add(estado);
            }
        }

        if (opciones.isEmpty()) {
            System.out.println("La orden " + orden.getId() + " está en " + actual
                    + " y ya no puede cambiar de estado.");
            return null;
        }

        System.out.println("Estado actual: " + actual);
        System.out.println("Nuevo estado:");
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + opciones.get(i));
        }
        int eleccion = leerEntero("Elija el estado: ", 1, opciones.size());
        return opciones.get(eleccion - 1);
    }

    /** Pide uno o más servicios para la orden (la lista nunca queda vacía). */
    private List<Servicio> leerServicios() {
        List<Servicio> servicios = new ArrayList<>();
        boolean agregarOtro = true;

        while (agregarOtro) {
            System.out.println("Servicio #" + (servicios.size() + 1) + ":");
            System.out.println("  1. Mantenimiento");
            System.out.println("  2. Reparación");
            int tipo = leerEntero("Elija el tipo: ", 1, 2);
            String nombre = leerTexto("Nombre del servicio: ");
            double precioBase = leerDecimal("Precio base ($): ");

            if (tipo == 1) {
                servicios.add(new Mantenimiento(nombre, precioBase));
            } else {
                double manoObra = leerDecimal("Costo de mano de obra ($): ");
                servicios.add(new Reparacion(nombre, precioBase, manoObra));
            }

            agregarOtro = leerSiNo("¿Agregar otro servicio? (s/n): ");
        }
        return servicios;
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

    /** Pide una cantidad de dinero (acepta punto o coma decimal) mayor o igual a 0. */
    private double leerDecimal(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje).replace(',', '.');
            try {
                double numero = Double.parseDouble(texto);
                if (numero >= 0 && Double.isFinite(numero)) {
                    return numero;
                }
            } catch (NumberFormatException e) {
                // No era un número: se vuelve a pedir.
            }
            System.out.println("Ingrese una cantidad válida, por ejemplo 25 o 25.50.");
        }
    }

    /** Pide una respuesta s/n y devuelve true si es "s". */
    private boolean leerSiNo(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje).toLowerCase();
            if (texto.equals("s")) {
                return true;
            }
            if (texto.equals("n")) {
                return false;
            }
            System.out.println("Responda s o n.");
        }
    }

    private String formatearDinero(double cantidad) {
        return String.format("%.2f", cantidad);
    }
}
