package com.octavemc.user;

import com.google.inject.Inject;
import com.octavemc.Apollo;
import com.octavemc.util.Dao;
import dev.morphia.Datastore;
import dev.morphia.query.experimental.filters.Filters;
import lombok.SneakyThrows;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UserDao extends BukkitRunnable implements Dao<UUID, User> {

    private final Datastore datastore;
    private final Map<UUID, User> cache;

    @Inject
    public UserDao(Datastore datastore) {
        this.datastore = datastore;
        this.cache = new ConcurrentHashMap<>();
        this.runTaskTimerAsynchronously(Apollo.getInstance(), 20L * 300, 20L * 300);
    }


    @SneakyThrows
    @Override
    public Optional<User> get(UUID identifier) {
        if (this.cache.containsKey(identifier)) {
            return Optional.of(this.cache.get(identifier));
        }
        var user = CompletableFuture.supplyAsync(() -> this.datastore.find(User.class).filter(Filters.eq("_id", identifier)).first()).join();
        if (user == null) {
            user = new User(identifier);
        }
        this.cache.putIfAbsent(identifier, user);
        return Optional.of(user);
    }

    @SneakyThrows
    @Override
    public List<User> getAll() {
        return CompletableFuture.supplyAsync(() -> this.datastore.find(User.class).iterator().toList()).get();
    }

    @Override
    public void update(User user) {
        this.datastore.merge(user);
    }

    @Override
    public void updateAll() {
        this.cache.values().forEach(user -> {
            User target = this.datastore.find(User.class).filter(Filters.eq("_id", user.getIdentifier())).first();
            if (target == null) {
                this.datastore.save(user);
            } else if (!target.equals(user)) {
                this.datastore.merge(user);
            }
        });
    }

    @Override
    public void save(User user) {
        this.datastore.save(user);
    }

    @Override
    public void delete(User user) {
        this.datastore
                .find(User.class)
                .filter(Filters.eq("_id", user.getIdentifier()))
                .delete();
    }

    public Map<UUID, User> getCache() {
        return cache;
    }

    @Override
    public void run() {
        this.updateAll();
        this.cache.values().removeIf(user -> user.getPlayer() == null);
    }
}
