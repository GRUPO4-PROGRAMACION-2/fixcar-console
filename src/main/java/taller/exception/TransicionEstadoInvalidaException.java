package taller.exception;

import taller.model.EstadoOrden;

public class TransicionEstadoInvalidaException extends TallerException {
    private static final long serialVersionUID = 1L;

    public TransicionEstadoInvalidaException(EstadoOrden actual, EstadoOrden pedido) {
        super("No se permite cambiar el estado de " + actual + " a " + pedido);
    }
}
