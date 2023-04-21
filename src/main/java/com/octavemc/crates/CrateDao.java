package com.octavemc.crates;

import com.octavemc.Apollo;
import com.octavemc.util.Dao;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Getter
public class CrateDao implements Dao<String, Crate> {

    private YamlConfiguration configuration;
    private HashMap<String, Crate> cache;

    public CrateDao(YamlConfiguration configuration) {
        this.configuration = configuration;
        this.cache = new HashMap<>();
    }

    @Override
    public void loadAll() {
        if (!this.configuration.isSet("crates")) return;
        this.configuration.getConfigurationSection("crates").getKeys(false).forEach(crate -> this.cache.putIfAbsent(crate, (Crate) configuration.get("crates." + crate)));
    }

    @SneakyThrows
    @Override
    public void saveAll() {
        this.cache.entrySet().forEach(entry -> this.configuration.set("crates." + entry.getKey(), entry.getValue()));
        this.configuration.save(new File(Apollo.getInstance().getDataFolder(), "crates.yml"));
    }

    @Override
    public Optional<Crate> get(@NonNull String identifier) {
        return this.cache.containsKey(identifier)
                ? Optional.of(this.cache.get(identifier))
                : Optional.empty();
    }

    @Override
    public List<Crate> getAll() {
        return (List<Crate>) this.cache.values();
    }

    public Optional<Crate> getByKey(ItemStack stack) {
        return this.cache.values().stream().filter(crate -> crate.getKey().isSimilar(stack)).findFirst();
    }

    public Optional<Crate> getByLocation(@NonNull Location location) {
        return this.cache.values().stream().filter(crate -> crate.getLocation().getLocation().equals(location)).findFirst();
    }

    @Override
    public void delete(Crate value) {
        this.configuration.set("crates." + value.getIdentifier(), null);
    }
}
