package taller.exception;

public class TallerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TallerException(String mensaje) {
        super(mensaje);
    }

    public TallerException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
