package taller.model;

public enum EstadoOrden {
    PENDIENTE,
    EN_PROCESO,
    FINALIZADA,
    CANCELADA;

    public boolean puedeCambiarA(EstadoOrden nuevoEstado) {
        switch (this) {
            case PENDIENTE:
                return nuevoEstado == EN_PROCESO || nuevoEstado == CANCELADA;
            case EN_PROCESO:
                return nuevoEstado == FINALIZADA || nuevoEstado == CANCELADA;
            default:
                return false;
        }
    }
}
