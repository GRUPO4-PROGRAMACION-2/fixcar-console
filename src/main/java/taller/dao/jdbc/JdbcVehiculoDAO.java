package taller.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import taller.dao.VehiculoDAO;
import taller.exception.TallerException;
import taller.model.Cliente;
import taller.model.Vehiculo;

public class JdbcVehiculoDAO implements VehiculoDAO {
    private static final String SELECT =
            "SELECT v.placa, v.marca, v.modelo, "
                    + "c.documento, c.nombre, c.telefono, c.correo, c.direccion "
                    + "FROM vehiculo v JOIN cliente c ON c.documento = v.cliente_documento";

    private final DataSource dataSource;

    public JdbcVehiculoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void guardar(Vehiculo vehiculo) throws TallerException {
        String sql = "INSERT INTO vehiculo (placa, marca, modelo, cliente_documento) VALUES (?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getPlaca());
            ps.setString(2, vehiculo.getMarca());
            ps.setString(3, vehiculo.getModelo());
            ps.setString(4, vehiculo.getCliente().getDocumento());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcErrores.alGuardar("vehículo", vehiculo.getPlaca(), e);
        }
    }

    @Override
    public Optional<Vehiculo> buscarPorId(String placa) throws TallerException {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE v.placa = ?")) {
            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcErrores.alConsultar("el vehículo " + placa, e);
        }
    }

    @Override
    public List<Vehiculo> listarTodos() throws TallerException {
        List<Vehiculo> vehiculos = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT + " ORDER BY v.placa");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                vehiculos.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw JdbcErrores.alConsultar("los vehículos", e);
        }
        return vehiculos;
    }

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        Cliente dueno = new Cliente(rs.getString("nombre"), rs.getString("documento"), rs.getString("telefono"),
                rs.getString("correo"), rs.getString("direccion"));
        return new Vehiculo(rs.getString("placa"), rs.getString("marca"), rs.getString("modelo"), dueno);
    }
}
