package taller.exception;

public class ConcurrenciaException extends TallerException {
    private static final long serialVersionUID = 1L;

    public ConcurrenciaException(String idOrden) {
        super("La orden " + idOrden + " fue modificada por otro proceso; vuelva a intentarlo");
    }
}
