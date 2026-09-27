package taller.service;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Procesa los diagnósticos técnicos de las órdenes en segundo plano (T08).
 *
 * El taller recibe varias órdenes y cada diagnóstico tarda lo mismo en estar
 * listo. Si el diagnóstico se hiciera en el hilo de la consola, el trabajador
 * no podría registrar otro cliente ni ver otra orden mientras espera. Por eso
 * cada orden se encola y un grupo pequeño de hilos (2 por defecto) la procesa
 * en paralelo; la consola sigue respondiendo porque encolar es inmediato.
 */
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

        // Hilos con nombre propio: al ver la salida del programa se sabe
        // qué hilo resolvió cada diagnóstico.
        AtomicInteger secuencia = new AtomicInteger();
        ThreadFactory fabrica = tarea -> {
            Thread hilo = new Thread(tarea, "procesador-ordenes-" + secuencia.incrementAndGet());
            hilo.setDaemon(false);
            return hilo;
        };

        this.ejecutor = Executors.newFixedThreadPool(cantidadHilos, fabrica);
    }

    /**
     * Encola el diagnóstico de una orden y devuelve la tarea para poder
     * consultar su resultado. Encolar no bloquea: la orden se guarda en la
     * cola del pool y un hilo libre la toma.
     */
    public Future<?> encolarDiagnostico(String idOrden) {
        if (idOrden == null || idOrden.isBlank()) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio.");
        }

        String idNormalizado = idOrden.trim();

        return ejecutor.submit(() -> {
            try {
                tallerService.procesarDiagnostico(idNormalizado);
            } catch (RuntimeException e) {
                // La orden se queda como estaba; se avisa sin tumbar el worker.
                System.err.println("[" + Thread.currentThread().getName() + "] "
                        + "No se pudo completar el diagnóstico de la orden " + idNormalizado
                        + ": " + e.getMessage());
            }
        });
    }

    /** Deja de aceptar órdenes y espera a que terminen las que ya están encoladas. */
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
