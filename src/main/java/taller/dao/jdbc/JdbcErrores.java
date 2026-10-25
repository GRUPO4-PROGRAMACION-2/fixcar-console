package taller.dao.jdbc;

import java.sql.SQLException;

import taller.exception.EntidadDuplicadaException;
import taller.exception.PersistenciaException;
import taller.exception.TallerException;

final class JdbcErrores {
    private static final String LLAVE_DUPLICADA = "23505";

    private JdbcErrores() {
    }

    static TallerException alGuardar(String entidad, String id, SQLException e) {
        if (LLAVE_DUPLICADA.equals(e.getSQLState())) {
            return new EntidadDuplicadaException(entidad, id);
        }
        return new PersistenciaException("No se pudo guardar " + entidad + " " + id, e);
    }

    static PersistenciaException alConsultar(String contexto, SQLException e) {
        return new PersistenciaException("Error al consultar " + contexto, e);
    }
}
