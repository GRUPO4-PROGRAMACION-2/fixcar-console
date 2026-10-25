package taller.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import taller.dto.ResultadoDiagnostico;
import taller.dto.ResumenLoteDTO;
import taller.exception.TallerException;
import taller.model.EstadoOrden;
import taller.model.OrdenTrabajo;

//Procesa diagnósticos en segundo plano usando un grupo acotado de hilos.
public final class ProcesadorOrdenes implements AutoCloseable {

    private static final int HILOS_POR_DEFECTO = 4;
    private static final long PAUSA_POR_DEFECTO_MS = 400;

    private final TallerService tallerService;
    private final int hilos;
    private final long pausaMs;
    private final ExecutorService ejecutor;

    // cola concurrente: escriben los hilos y lee la consola
    private final Queue<ResultadoDiagnostico> notificaciones = new ConcurrentLinkedQueue<>();

    public ProcesadorOrdenes(TallerService tallerService) {
        this(tallerService, HILOS_POR_DEFECTO, PAUSA_POR_DEFECTO_MS);
    }

    public ProcesadorOrdenes(TallerService tallerService, int cantidadHilos, long pausaMs) {
        this.tallerService = Objects.requireNonNull(tallerService,
                "El servicio del taller no puede ser nulo.");
        if (cantidadHilos < 1) {
            throw new IllegalArgumentException("Debe haber al menos un hilo.");
        }
        if (pausaMs < 0) {
            throw new IllegalArgumentException("La pausa no puede ser negativa.");
        }
        this.hilos = cantidadHilos;
        this.pausaMs = pausaMs;

        AtomicInteger secuencia = new AtomicInteger();
        ThreadFactory fabrica = tarea -> {
            Thread hilo = new Thread(tarea, "procesador-ordenes-" + secuencia.incrementAndGet());
            hilo.setDaemon(false);
            return hilo;
        };
        this.ejecutor = Executors.newFixedThreadPool(cantidadHilos, fabrica);
    }

    //Encola el diagnóstico y devuelve una tarea para consultar su resultado.
    public Future<ResultadoDiagnostico> encolarDiagnostico(String idOrden, EstadoOrden nuevoEstado) {
        if (idOrden == null || idOrden.isBlank()) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        }
        Objects.requireNonNull(nuevoEstado, "El estado de destino no puede ser nulo.");
        String id = idOrden.trim();
        return ejecutor.submit(() -> {
            ResultadoDiagnostico resultado = diagnosticar(id, nuevoEstado);
            notificaciones.add(resultado);
            return resultado;
        });
    }

    //Diagnostica todas las pendientes a la vez.
    public ResumenLoteDTO diagnosticarPendientes() {
        List<OrdenTrabajo> pendientes = tallerService.listarOrdenesPendientes();
        List<Callable<ResultadoDiagnostico>> tareas = new ArrayList<>();
        for (OrdenTrabajo orden : pendientes) {
            String id = orden.getId();
            tareas.add(() -> diagnosticar(id, EstadoOrden.EN_PROCESO));
        }

        long inicio = System.nanoTime();
        List<ResultadoDiagnostico> detalle = new ArrayList<>();
        try {
            List<Future<ResultadoDiagnostico>> futuros = ejecutor.invokeAll(tareas);
            for (int i = 0; i < futuros.size(); i++) {
                detalle.add(esperar(futuros.get(i), pendientes.get(i).getId()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TallerException("El diagnóstico masivo fue interrumpido", e);
        }
        long duracion = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);

        int exitosos = (int) detalle.stream().filter(ResultadoDiagnostico::exitoso).count();
        return new ResumenLoteDTO(detalle.size(), exitosos, detalle.size() - exitosos, duracion, hilos, detalle);
    }

    public List<ResultadoDiagnostico> retirarNotificaciones() {
        List<ResultadoDiagnostico> resultados = new ArrayList<>();
        ResultadoDiagnostico resultado;
        while ((resultado = notificaciones.poll()) != null) {
            resultados.add(resultado);
        }
        return resultados;
    }

    //Deja de aceptar órdenes y espera a que las ya encoladas terminen.

    @Override
    public void close() {
        ejecutor.shutdown();
        try {
            if (!ejecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                ejecutor.shutdownNow();
                ejecutor.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            ejecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private ResultadoDiagnostico diagnosticar(String idOrden, EstadoOrden nuevoEstado) {
        long inicio = System.nanoTime();
        String hilo = Thread.currentThread().getName();
        try {
            Thread.sleep(pausaMs); // simula el trabajo del mecánico
            tallerService.procesarDiagnostico(idOrden, nuevoEstado);
            return new ResultadoDiagnostico(idOrden, true, "Ahora está en " + nuevoEstado,
                    milisDesde(inicio), hilo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ResultadoDiagnostico(idOrden, false, "Diagnóstico interrumpido", milisDesde(inicio), hilo);
        } catch (TallerException e) {
            return new ResultadoDiagnostico(idOrden, false, e.getMessage(), milisDesde(inicio), hilo);
        } catch (RuntimeException e) {
            return new ResultadoDiagnostico(idOrden, false, "Error inesperado: " + e, milisDesde(inicio), hilo);
        }
    }

    private ResultadoDiagnostico esperar(Future<ResultadoDiagnostico> futuro, String idOrden)
            throws InterruptedException {
        try {
            return futuro.get();
        } catch (ExecutionException e) {
            return new ResultadoDiagnostico(idOrden, false, "Error inesperado: " + e.getCause(), 0, "-");
        }
    }

    private static long milisDesde(long inicioNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioNanos);
    }
}
