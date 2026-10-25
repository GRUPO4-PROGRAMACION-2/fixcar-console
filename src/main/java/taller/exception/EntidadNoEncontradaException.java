package taller.exception;

public class EntidadNoEncontradaException extends TallerException {
    private static final long serialVersionUID = 1L;

    public EntidadNoEncontradaException(String entidad, String identificador) {
        super("No se encontró " + entidad + " con identificador: " + identificador);
    }
}
