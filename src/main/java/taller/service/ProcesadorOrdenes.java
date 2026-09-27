package taller.service;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import taller.model.EstadoOrden;

//Procesa diagnósticos en segundo plano usando un grupo acotado de hilos.
public final class ProcesadorOrdenes implements AutoCloseable {

    private static final int HILOS_POR_DEFECTO = 2;

    private final TallerService tallerService;
    private final ExecutorService ejecutor;

    public ProcesadorOrdenes(TallerService tallerService) {
        this(tallerService, HILOS_POR_DEFECTO);
    }

    public ProcesadorOrdenes(TallerService tallerService, int cantidadHilos) {
        this.tallerService = Objects.requireNonNull(tallerService,
                "El servicio del taller no puede ser nulo.");
        if (cantidadHilos < 1) {
            throw new IllegalArgumentException("Debe haber al menos un hilo.");
        }

        AtomicInteger secuencia = new AtomicInteger();
        ThreadFactory fabrica = tarea -> {
            Thread hilo = new Thread(tarea, "procesador-ordenes-" + secuencia.incrementAndGet());
            hilo.setDaemon(false);
            return hilo;
        };
        this.ejecutor = Executors.newFixedThreadPool(cantidadHilos, fabrica);
    }

    //Encola el diagnóstico y devuelve una tarea para consultar su resultado. 
    
    public Future<?> encolarDiagnostico(String idOrden, EstadoOrden nuevoEstado) {
        if (idOrden == null || idOrden.isBlank()) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        }
        Objects.requireNonNull(nuevoEstado, "El estado de destino no puede ser nulo.");
        String idNormalizado = idOrden.trim();
        return ejecutor.submit(() -> tallerService.procesarDiagnostico(idNormalizado, nuevoEstado));
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
}
