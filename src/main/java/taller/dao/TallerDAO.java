package taller.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class TallerDAO {

    private static final Path ARCHIVO = Path.of("data", "taller.dat");

    private static final Object LOCK = new Object();

    public TallerDAO() {
        crearArchivoSiNoExiste();
    }

    public TallerData cargar() {

        synchronized (LOCK) {

            if (!Files.exists(ARCHIVO)) {
                TallerData datos = new TallerData();
                guardar(datos);
                return datos;
            }

            try (
                    InputStream entrada = Files.newInputStream(ARCHIVO);
                    ObjectInputStream objetoEntrada =
                            new ObjectInputStream(entrada)
            ) {

                Object objeto = objetoEntrada.readObject();

                if (!(objeto instanceof TallerData)) {
                    throw new IOException(
                            "El archivo no contiene datos válidos del taller."
                    );
                }

                return (TallerData) objeto;

            } catch (IOException | ClassNotFoundException e) {

                throw new RuntimeException(
                        "No se pudieron cargar los datos del taller.",
                        e
                );
            }
        }
    }

    public void guardar(TallerData datos) {

        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del taller no pueden ser nulos."
            );
        }

        synchronized (LOCK) {

            Path archivoTemporal = null;

            try {

                Files.createDirectories(ARCHIVO.getParent());

                // Primero se escribe en un archivo temporal y al final se
                // reemplaza taller.dat. Si el programa se interrumpe a mitad de
                // la escritura, el archivo anterior queda intacto en lugar de
                // quedar un taller.dat dañado que no se puede leer después.
                archivoTemporal = Files.createTempFile(
                        ARCHIVO.getParent(),
                        "taller",
                        ".tmp"
                );

                try (
                        OutputStream salida = Files.newOutputStream(archivoTemporal);
                        ObjectOutputStream objetoSalida =
                                new ObjectOutputStream(salida)
                ) {

                    objetoSalida.writeObject(datos);
                    objetoSalida.flush();
                }

                reemplazarArchivo(archivoTemporal);
                archivoTemporal = null;

            } catch (IOException e) {

                throw new RuntimeException(
                        "No se pudieron guardar los datos del taller.",
                        e
                );

            } finally {

                // Si algo falló, el temporal no se deja abandonado en data/.
                if (archivoTemporal != null) {
                    try {
                        Files.deleteIfExists(archivoTemporal);
                    } catch (IOException ignorada) {
                        // No afecta la operación ya fallida.
                    }
                }
            }
        }
    }

    /** Renombra el temporal sobre taller.dat, de forma atómica si el sistema la admite. */
    private void reemplazarArchivo(Path archivoTemporal) throws IOException {

        try {
            Files.move(
                    archivoTemporal,
                    ARCHIVO,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                    archivoTemporal,
                    ARCHIVO,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    /**
     * Aplica una modificación mientras mantiene bloqueado el ciclo completo
     * de lectura y escritura, evitando que operaciones concurrentes se pisen.
     */
    public void actualizarDatos(Consumer<TallerData> actualizacion) {
        if (actualizacion == null) {
            throw new IllegalArgumentException("La actualización no puede ser nula.");
        }

        synchronized (LOCK) {
            TallerData datos = cargar();
            actualizacion.accept(datos);
            guardar(datos);
        }
    }

    private void crearArchivoSiNoExiste() {

        synchronized (LOCK) {

            if (Files.exists(ARCHIVO)) {
                return;
            }

            // Se reutiliza guardar() para que el primer taller.dat se cree con
            // el mismo mecanismo (escritura temporal + reemplazo) que el resto.
            guardar(new TallerData());
        }
    }
}
