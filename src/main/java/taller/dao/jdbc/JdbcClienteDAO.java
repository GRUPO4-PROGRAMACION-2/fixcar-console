package taller.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import taller.dao.ClienteDAO;
import taller.exception.TallerException;
import taller.model.Cliente;

public class JdbcClienteDAO implements ClienteDAO {
    private static final String SELECT =
            "SELECT documento, nombre, telefono, correo, direccion FROM cliente";

    private final DataSource dataSource;

    public JdbcClienteDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void guardar(Cliente cliente) throws TallerException {
        String sql = "INSERT INTO cliente (documento, nombre, telefono, correo, direccion) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cliente.getDocumento());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getCorreo());
            ps.setString(5, cliente.getDireccion());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcErrores.alGuardar("cliente", cliente.getDocumento(), e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(String documento) throws TallerException {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE documento = ?")) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcErrores.alConsultar("el cliente " + documento, e);
        }
    }

    @Override
    public List<Cliente> listarTodos() throws TallerException {
        List<Cliente> clientes = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + " ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw JdbcErrores.alConsultar("los clientes", e);
        }
        return clientes;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getString("nombre"), rs.getString("documento"), rs.getString("telefono"),
                rs.getString("correo"), rs.getString("direccion"));
    }
}
