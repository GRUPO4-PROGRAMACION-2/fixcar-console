package taller;

import java.io.Console;
import java.nio.charset.Charset;
import java.util.Scanner;

import taller.config.DataSourceConfig;
import taller.controller.TallerController;
import taller.dao.jdbc.JdbcClienteDAO;
import taller.dao.jdbc.JdbcOrdenDAO;
import taller.dao.jdbc.JdbcTrabajadorDAO;
import taller.dao.jdbc.JdbcVehiculoDAO;
import taller.exception.TallerException;
import taller.service.ProcesadorOrdenes;
import taller.service.TallerService;
import taller.ui.ConsoleUI;

/**
 * Punto de arranque (T10).
 * Crea las capas de abajo hacia arriba, inicia la consola
 * y al salir cierra los hilos del procesador y el pool de conexiones.
 */
public class Main {

    public static void main(String[] args) {
        org.apache.tomcat.jdbc.pool.DataSource dataSource = DataSourceConfig.crear();
        try {
            DataSourceConfig.inicializarBaseDeDatos(dataSource);

            TallerService service = new TallerService(
                    new JdbcClienteDAO(dataSource),
                    new JdbcVehiculoDAO(dataSource),
                    new JdbcTrabajadorDAO(dataSource),
                    new JdbcOrdenDAO(dataSource));
            ProcesadorOrdenes procesador = new ProcesadorOrdenes(service);

            Scanner scanner = new Scanner(System.in, charsetDeConsola());

            // try-with-resources: al terminar el bloque, Java llama automáticamente
            // a controller.close(), que cierra los hilos del procesador,
            // incluso si ocurre un error.
            try (TallerController controller = new TallerController(service, procesador)) {
                new ConsoleUI(controller, scanner).iniciar();
                System.out.println("Esperando a que terminen los diagnósticos pendientes...");
            }
        } catch (TallerException e) {
            System.err.println("No se pudo iniciar el sistema: " + e.getMessage());
        } finally {
            dataSource.close();
        }
        System.out.println("Sistema cerrado correctamente.");
    }

    /**
     * En Windows la consola no usa UTF-8 por defecto, pero Java 21 sí.
     * Si no se indica el charset de la consola, letras como "ñ" o "á"
     * que escribe el usuario llegan dañadas (por ejemplo "Mu?oz").
     */
    private static Charset charsetDeConsola() {
        Console consola = System.console();
        if (consola != null) {
            return consola.charset();
        }
        return Charset.defaultCharset();
    }
}
