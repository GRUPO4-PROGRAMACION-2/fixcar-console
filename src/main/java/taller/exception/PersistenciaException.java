package taller.exception;

import java.sql.SQLException;

public class PersistenciaException extends TallerException {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje, SQLException causa) {
        super(mensaje, causa);
    }
}
