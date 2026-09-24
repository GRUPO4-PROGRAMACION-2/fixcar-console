package taller.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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

            try {

                Files.createDirectories(ARCHIVO.getParent());

                try (
                        OutputStream salida = Files.newOutputStream(ARCHIVO);
                        ObjectOutputStream objetoSalida =
                                new ObjectOutputStream(salida)
                ) {

                    objetoSalida.writeObject(datos);
                    objetoSalida.flush();
                }

            } catch (IOException e) {

                throw new RuntimeException(
                        "No se pudieron guardar los datos del taller.",
                        e
                );
            }
        }
    }

    private void crearArchivoSiNoExiste() {

        synchronized (LOCK) {

            try {

                Files.createDirectories(ARCHIVO.getParent());

                if (!Files.exists(ARCHIVO)) {

                    TallerData datos = new TallerData();

                    try (
                            OutputStream salida =
                                    Files.newOutputStream(ARCHIVO);
                            ObjectOutputStream objetoSalida =
                                    new ObjectOutputStream(salida)
                    ) {

                        objetoSalida.writeObject(datos);
                        objetoSalida.flush();
                    }
                }

            } catch (IOException e) {

                throw new RuntimeException(
                        "No se pudo crear el archivo de datos.",
                        e
                );
            }
        }
    }
}
    