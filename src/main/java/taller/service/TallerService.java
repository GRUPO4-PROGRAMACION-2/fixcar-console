package taller.service;

import taller.dao.TallerDAO;
import taller.dao.TallerData;
import taller.model.Cliente;
import taller.model.EstadoOrden;
import taller.model.OrdenTrabajo;
import taller.model.Servicio;
import taller.model.Trabajador;
import taller.model.Vehiculo;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lógica de negocio del taller (T07).
 *
 * Reglas que se aplican aquí y en ningún otro lado:
 *  - un documento identifica a un solo cliente y una placa a un solo vehículo;
 *  - el identificador de la orden lo genera el servicio, no la consola;
 *  - toda alta o cambio de estado se persiste de inmediato en taller.dat;
 *  - el diagnóstico técnico es una operación de negocio real, por eso el
 *    procesador de segundo plano (T08) la delega a esta clase.
 *
 * No conoce Scanner ni imprime menús: recibe datos y devuelve resultados.
 */
public class TallerService {

    private static final String PREFIJO_ID_ORDEN = "O-";

    /**
     * Tiempo que se simula como si el taller estuviera inspeccionando el
     * vehículo. Representa el trabajo real del diagnóstico (lectura de fallas,
     * revisión del servicio contratado) y es la razón técnica por la que esa
     * operación se ejecuta en un hilo aparte y no en el hilo de la consola.
     */
    private static final long TIEMPO_SIMULADO_DIAGNOSTICO_MS = 700;

    private final TallerDAO tallerDAO;

    public TallerService(TallerDAO tallerDAO) {
        if (tallerDAO == null) {
            throw new IllegalArgumentException("El DAO no puede ser nulo.");
        }
        this.tallerDAO = tallerDAO;
    }

    // ===================== Clientes y vehículos =====================

    public void registrarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }

        tallerDAO.actualizarDatos(datos -> {
            if (datos.buscarClientePorDocumento(cliente.getDocumento()).isPresent()) {
                throw new IllegalArgumentException(
                        "Ya existe un cliente registrado con el documento " + cliente.getDocumento());
            }
            datos.agregarCliente(cliente);
        });
    }

    public void registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo.");
        }

        tallerDAO.actualizarDatos(datos -> {
            if (datos.buscarVehiculoPorPlaca(vehiculo.getPlaca()).isPresent()) {
                throw new IllegalArgumentException(
                        "Ya existe un vehículo registrado con la placa " + vehiculo.getPlaca());
            }
            datos.agregarVehiculo(vehiculo);
        });
    }

    public Optional<Cliente> buscarClientePorDocumento(String documento) {
        return tallerDAO.cargar().buscarClientePorDocumento(documento);
    }

    public Optional<Vehiculo> buscarVehiculoPorPlaca(String placa) {
        return tallerDAO.cargar().buscarVehiculoPorPlaca(placa);
    }

    public List<Cliente> listarClientes() {
        return tallerDAO.cargar().consultarClientes();
    }

    public List<Vehiculo> listarVehiculos() {
        return tallerDAO.cargar().consultarVehiculos();
    }

    // ===================== Órdenes de trabajo =====================

    /**
     * Crea y guarda una orden. El identificador lo genera el servicio para que
     * la consola no tenga que conocer la regla de la numeración.
     */
    public OrdenTrabajo crearOrden(String descripcion, Cliente cliente, Vehiculo vehiculo,
                                   Trabajador trabajador, List<Servicio> servicios) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehículo no puede ser nulo.");
        }
        if (trabajador == null) {
            throw new IllegalArgumentException("El trabajador no puede ser nulo.");
        }
        if (servicios == null || servicios.isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un servicio.");
        }
        for (Servicio servicio : servicios) {
            if (servicio == null) {
                throw new IllegalArgumentException("La lista de servicios contiene un valor nulo.");
            }
        }

        AtomicReference<OrdenTrabajo> ordenCreada = new AtomicReference<>();
        tallerDAO.actualizarDatos(datos -> {
            OrdenTrabajo orden = new OrdenTrabajo(
                    siguienteIdOrden(datos), descripcion, cliente, vehiculo, trabajador);
            for (Servicio servicio : servicios) {
                orden.agregarServicio(servicio);
            }
            datos.agregarOrden(orden);
            ordenCreada.set(orden);
        });

        return ordenCreada.get();
    }

    public List<OrdenTrabajo> listarOrdenes() {
        return tallerDAO.cargar().consultarOrdenes();
    }

    public Optional<OrdenTrabajo> buscarOrden(String id) {
        return tallerDAO.cargar().buscarOrden(id);
    }

    public void cambiarEstadoOrden(String idOrden, EstadoOrden nuevoEstado) {
        if (idOrden == null || idOrden.isBlank()) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        }
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo.");
        }

        String idNormalizado = idOrden.trim();
        tallerDAO.actualizarDatos(datos -> {
            OrdenTrabajo ordenEncontrada = datos.buscarOrden(idNormalizado)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró la orden con ID: " + idOrden));

            ordenEncontrada.cambiarEstado(nuevoEstado);
        });
    }

    // ===================== Diagnóstico técnico (operación de T08) =====================

    /**
     * Diagnostica una orden: revisa el vehículo, calcula el costo de los
     * servicios y deja el resultado registrado en la orden, que pasa de
     * PENDIENTE a EN_PROCESO.
     *
     * Esta es la operación que ProcesadorOrdenes ejecuta en segundo plano.
     */
    public void procesarDiagnostico(String idOrden) {
        if (idOrden == null || idOrden.isBlank()) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        }

        String id = idOrden.trim();
        OrdenTrabajo orden = buscarOrden(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la orden con ID: " + id));

        if (orden.getEstado() != EstadoOrden.PENDIENTE) {
            throw new IllegalStateException(
                    "La orden " + id + " solo se puede diagnosticar cuando está PENDIENTE; está en " + orden.getEstado());
        }

        // Trabajo de taller: la consola no debe esperar aquí.
        simularInspeccion(id);

        String textoDiagnostico = construirDiagnostico(orden);

        tallerDAO.actualizarDatos(datos -> {
            OrdenTrabajo ordenEnDisco = datos.buscarOrden(id)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró la orden con ID: " + id));
            ordenEnDisco.registrarDiagnostico(textoDiagnostico);
        });
    }

    private void simularInspeccion(String idOrden) {
        try {
            Thread.sleep(TIEMPO_SIMULADO_DIAGNOSTICO_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Se interrumpió el diagnóstico de la orden " + idOrden);
        }
    }

    /** Arma el texto del diagnóstico usando el costo que calcula cada tipo de servicio. */
    private String construirDiagnostico(OrdenTrabajo orden) {
        int cantidadServicios = orden.getServicios().size();

        StringBuilder texto = new StringBuilder();
        texto.append("Inspección completada para el vehículo ")
                .append(orden.getVehiculo().getMarca()).append(' ')
                .append(orden.getVehiculo().getModelo())
                .append(" (placa ").append(orden.getVehiculo().getPlaca()).append("). ")
                .append("Se ").append(cantidadServicios == 1 ? "confirma" : "confirman")
                .append(' ').append(cantidadServicios)
                .append(cantidadServicios == 1 ? " servicio" : " servicios")
                .append(" por un total estimado de $")
                .append(String.format("%.2f", orden.calcularCostoTotal()))
                .append(". Responsable: ").append(orden.getTrabajador().getNombre())
                .append(". La orden queda lista para iniciar la reparación.");

        return texto.toString();
    }

    /** O-1, O-2, O-3... calculado sobre las órdenes ya guardadas. */
    private String siguienteIdOrden(TallerData datos) {
        int mayor = 0;

        for (OrdenTrabajo orden : datos.consultarOrdenes()) {
            String id = orden.getId();
            if (id != null && id.startsWith(PREFIJO_ID_ORDEN)) {
                try {
                    mayor = Math.max(mayor, Integer.parseInt(id.substring(PREFIJO_ID_ORDEN.length())));
                } catch (NumberFormatException e) {
                    // Un identificador con otro formato no participa en la secuencia.
                }
            }
        }

        return PREFIJO_ID_ORDEN + (mayor + 1);
    }
}
