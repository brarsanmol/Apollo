package com.octavemc.util;

import java.util.List;
import java.util.Optional;

public interface Dao<K, V> {

    Optional<V> get(K identifier);

    List<V> getAll();

    default List<? extends V> getAll(V value)  {
        throw new UnsupportedOperationException("Get All By Type is not supported on this DAO!");
    }

    default void loadAll() {
        throw new UnsupportedOperationException("Load all is not supported on this DAO!");
    }

    default void update(V value) {
        throw new UnsupportedOperationException("Update is not supported on this DAO!");
    }

    default void updateAll() {
        throw new UnsupportedOperationException("Update all is not supported on this DAO!");
    }

    default void save(V value) {
        throw new UnsupportedOperationException("Save is not supported on this DAO!");
    }

    default void saveAll() {
        throw new UnsupportedOperationException("Save all is not supported on this DAO!");
    }

    default void delete(V value)  {
        throw new UnsupportedOperationException("Delete is not supported on this DAO!");
    }

}
