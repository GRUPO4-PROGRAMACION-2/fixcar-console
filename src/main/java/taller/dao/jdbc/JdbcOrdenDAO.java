package taller.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import taller.dao.OrdenDAO;
import taller.exception.TallerException;
import taller.model.Cliente;
import taller.model.EstadoOrden;
import taller.model.Mantenimiento;
import taller.model.OrdenTrabajo;
import taller.model.Reparacion;
import taller.model.Servicio;
import taller.model.Trabajador;
import taller.model.Vehiculo;

public class JdbcOrdenDAO implements OrdenDAO {

    private static final String SELECT_ORDEN =
            "SELECT o.id, o.descripcion, o.estado, o.fecha_creacion, "
                    + "c.documento AS c_documento, c.nombre AS c_nombre, c.telefono AS c_telefono, "
                    + "c.correo AS c_correo, c.direccion AS c_direccion, "
                    + "v.placa, v.marca, v.modelo, "
                    + "t.documento AS t_documento, t.nombre AS t_nombre, t.telefono AS t_telefono, "
                    + "t.cargo, t.especialidad "
                    + "FROM orden_trabajo o "
                    + "JOIN cliente c ON c.documento = o.cliente_documento "
                    + "JOIN vehiculo v ON v.placa = o.vehiculo_placa "
                    + "JOIN trabajador t ON t.documento = o.trabajador_documento";

    private final DataSource dataSource;

    public JdbcOrdenDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void guardar(OrdenTrabajo orden) throws TallerException {
        String sqlOrden = "INSERT INTO orden_trabajo (id, descripcion, cliente_documento, vehiculo_placa, "
                + "trabajador_documento, estado, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlServicio = "INSERT INTO servicio_orden (orden_id, tipo, nombre, precio_base, costo_mano_obra) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection()) {
            // transaccion: la orden y sus servicios se guardan juntos
            con.setAutoCommit(false);
            try (PreparedStatement psOrden = con.prepareStatement(sqlOrden);
                 PreparedStatement psServicio = con.prepareStatement(sqlServicio)) {

                psOrden.setString(1, orden.getId());
                psOrden.setString(2, orden.getDescripcion());
                psOrden.setString(3, orden.getCliente().getDocumento());
                psOrden.setString(4, orden.getVehiculo().getPlaca());
                psOrden.setString(5, orden.getTrabajador().getDocumento());
                psOrden.setString(6, orden.getEstado().name());
                psOrden.setTimestamp(7, Timestamp.valueOf(orden.getFechaCreacion()));
                psOrden.executeUpdate();

                for (Servicio servicio : orden.getServicios()) {
                    psServicio.setString(1, orden.getId());
                    psServicio.setString(2, servicio.getTipo());
                    psServicio.setString(3, servicio.getNombre());
                    psServicio.setDouble(4, servicio.getPrecioBase());
                    psServicio.setDouble(5, servicio.getCostoManoObra());
                    psServicio.addBatch();
                }
                psServicio.executeBatch();
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw JdbcErrores.alGuardar("orden", orden.getId(), e);
        }
    }

    @Override
    public Optional<OrdenTrabajo> buscarPorId(String id) throws TallerException {
        List<OrdenTrabajo> ordenes = consultar(" WHERE o.id = ?", id);
        return ordenes.isEmpty() ? Optional.empty() : Optional.of(ordenes.get(0));
    }

    @Override
    public List<OrdenTrabajo> listarTodos() throws TallerException {
        return consultar("");
    }

    @Override
    public List<OrdenTrabajo> listarPorEstado(EstadoOrden estado) throws TallerException {
        return consultar(" WHERE o.estado = ?", estado.name());
    }

    // bloqueo optimista: solo actualiza si la orden sigue en el estado esperado
    @Override
    public boolean actualizarEstado(String idOrden, EstadoOrden esperado, EstadoOrden nuevo) throws TallerException {
        String sql = "UPDATE orden_trabajo SET estado = ? WHERE id = ? AND estado = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevo.name());
            ps.setString(2, idOrden);
            ps.setString(3, esperado.name());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw JdbcErrores.alGuardar("orden", idOrden, e);
        }
    }

    private List<OrdenTrabajo> consultar(String filtro, String... parametros) throws TallerException {
        List<OrdenTrabajo> ordenes = new ArrayList<>();
        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    SELECT_ORDEN + filtro + " ORDER BY o.fecha_creacion, o.id")) {
                for (int i = 0; i < parametros.length; i++) {
                    ps.setString(i + 1, parametros[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ordenes.add(mapearOrden(rs));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT tipo, nombre, precio_base, costo_mano_obra FROM servicio_orden "
                            + "WHERE orden_id = ? ORDER BY id")) {
                for (OrdenTrabajo orden : ordenes) {
                    ps.setString(1, orden.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            orden.agregarServicio(mapearServicio(rs));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw JdbcErrores.alConsultar("las órdenes", e);
        }
        return ordenes;
    }

    private OrdenTrabajo mapearOrden(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente(rs.getString("c_nombre"), rs.getString("c_documento"),
                rs.getString("c_telefono"), rs.getString("c_correo"), rs.getString("c_direccion"));
        Vehiculo vehiculo = new Vehiculo(rs.getString("placa"), rs.getString("marca"),
                rs.getString("modelo"), cliente);
        Trabajador trabajador = new Trabajador(rs.getString("t_nombre"), rs.getString("t_documento"),
                rs.getString("t_telefono"), rs.getString("cargo"), rs.getString("especialidad"));
        return new OrdenTrabajo(rs.getString("id"), rs.getString("descripcion"), cliente, vehiculo, trabajador,
                EstadoOrden.valueOf(rs.getString("estado")),
                rs.getTimestamp("fecha_creacion").toLocalDateTime());
    }

    private Servicio mapearServicio(ResultSet rs) throws SQLException {
        String tipo = rs.getString("tipo");
        String nombre = rs.getString("nombre");
        double precioBase = rs.getDouble("precio_base");
        return switch (tipo) {
            case "MANTENIMIENTO" -> new Mantenimiento(nombre, precioBase);
            case "REPARACION" -> new Reparacion(nombre, precioBase, rs.getDouble("costo_mano_obra"));
            default -> throw new SQLException("Tipo de servicio desconocido: " + tipo);
        };
    }
}
