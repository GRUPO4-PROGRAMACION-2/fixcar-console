package taller;

import java.io.Console;
import java.nio.charset.Charset;
import java.util.Scanner;

import taller.controller.TallerController;
import taller.dao.TallerDAO;
import taller.service.ProcesadorOrdenes;
import taller.service.TallerService;
import taller.ui.ConsoleUI;

/**
 * Punto de arranque (T10).
 * Crea las capas de abajo hacia arriba, inicia la consola
 * y al salir cierra los hilos del procesador.
 */
public class Main {

    public static void main(String[] args) {
        // Constructores propuestos: confirmar las firmas con T06, T07, T08 y T09.
        TallerDAO dao = new TallerDAO();
        TallerService service = new TallerService(dao);
        ProcesadorOrdenes procesador = new ProcesadorOrdenes(service);
        TallerController controller = new TallerController(service, procesador);

        Scanner scanner = new Scanner(System.in, charsetDeConsola());

        try {
            new ConsoleUI(controller, scanner).iniciar();
        } finally {
            // finally se ejecuta aunque ocurra un error,
            // así los hilos siempre se cierran y el programa termina limpio.
            procesador.cerrar();
        }
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
