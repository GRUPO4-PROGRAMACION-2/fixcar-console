package taller.dao;

import java.util.List;
import java.util.Optional;

import taller.exception.TallerException;

public interface Repository<T, ID> {
    void guardar(T entidad) throws TallerException;

    Optional<T> buscarPorId(ID id) throws TallerException;

    List<T> listarTodos() throws TallerException;

    default boolean existe(ID id) throws TallerException {
        return buscarPorId(id).isPresent();
    }
}
