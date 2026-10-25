package taller.dao;

import java.util.List;

import taller.exception.TallerException;
import taller.model.EstadoOrden;
import taller.model.OrdenTrabajo;

public interface OrdenDAO extends Repository<OrdenTrabajo, String> {

    List<OrdenTrabajo> listarPorEstado(EstadoOrden estado) throws TallerException;

    boolean actualizarEstado(String idOrden, EstadoOrden esperado, EstadoOrden nuevo) throws TallerException;
}
