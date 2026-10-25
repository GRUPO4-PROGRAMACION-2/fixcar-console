package taller.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import taller.dao.TrabajadorDAO;
import taller.exception.TallerException;
import taller.model.Trabajador;

public class JdbcTrabajadorDAO implements TrabajadorDAO {
    private static final String SELECT =
            "SELECT documento, nombre, telefono, cargo, especialidad FROM trabajador";

    private final DataSource dataSource;

    public JdbcTrabajadorDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void guardar(Trabajador trabajador) throws TallerException {
        String sql = "INSERT INTO trabajador (documento, nombre, telefono, cargo, especialidad) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trabajador.getDocumento());
            ps.setString(2, trabajador.getNombre());
            ps.setString(3, trabajador.getTelefono());
            ps.setString(4, trabajador.getCargo());
            ps.setString(5, trabajador.getEspecialidad());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcErrores.alGuardar("trabajador", trabajador.getDocumento(), e);
        }
    }

    @Override
    public Optional<Trabajador> buscarPorId(String documento) throws TallerException {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE documento = ?")) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcErrores.alConsultar("el trabajador " + documento, e);
        }
    }

    @Override
    public List<Trabajador> listarTodos() throws TallerException {
        List<Trabajador> trabajadores = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + " ORDER BY documento");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                trabajadores.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw JdbcErrores.alConsultar("los trabajadores", e);
        }
        return trabajadores;
    }

    private Trabajador mapear(ResultSet rs) throws SQLException {
        return new Trabajador(rs.getString("nombre"), rs.getString("documento"), rs.getString("telefono"),
                rs.getString("cargo"), rs.getString("especialidad"));
    }
}
