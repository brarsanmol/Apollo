package com.octavemc.economy.merchants;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.octavemc.Apollo;
import com.octavemc.util.Dao;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class MerchantDao implements Dao<String, MerchantEntityVillager> {

    private YamlConfiguration configuration;
    private HashMap<String, MerchantEntityVillager> cache;

    @Inject
    public MerchantDao(@Named("MerchantsYamlConfiguration") YamlConfiguration configuration) {
        this.configuration = configuration;
        this.cache = new HashMap<>();
    }

    @Override
    public void loadAll() {
        if (!this.configuration.isSet("merchants")) return;
        this.configuration.getConfigurationSection("merchants").getKeys(false).forEach(name -> {
            var section = (Map<String, Object>)  this.configuration.get("merchants." + name);
            var location = new Location(
                    Apollo.getInstance().getServer().getWorld((String) section.get("location.world")),
                    Double.parseDouble((String) section.get("location.x")),
                    Double.parseDouble((String) section.get("location.y")),
                    Double.parseDouble((String) section.get("location.z")),
                    Float.parseFloat((String) section.get("location.yaw")),
                    Float.parseFloat((String) section.get("location.pitch"))
            );

            var merchant = new MerchantEntityVillager(location, (String) section.get("name"), MerchantType.valueOf((String) section.get("type")));
            this.cache.putIfAbsent(name, merchant);
        });

        this.cache.values().forEach(System.out::println);
    }



    @SneakyThrows
    @Override
    public void saveAll() {
        this.cache.entrySet().forEach(entry -> this.configuration.set("merchants." + entry.getKey(), entry.getValue()));
        this.configuration.save(new File(Apollo.getInstance().getDataFolder(), "merchants.yml"));
    }

    @Override
    public Optional<MerchantEntityVillager> get(@NonNull String identifier) {
        return this.cache.containsKey(identifier)
                ? Optional.of(this.cache.get(identifier))
                : Optional.empty();
    }

    @Override
    public List<MerchantEntityVillager> getAll() {
        return (List<MerchantEntityVillager>) this.cache.values();
    }

    public Optional<MerchantEntityVillager> getByLocation(@NonNull Location location) {
        return this.cache.values().stream().filter(merchant -> merchant.getBukkitEntity().getLocation().equals(location)).findFirst();
    }

    @Override
    public void delete(MerchantEntityVillager value) {
        this.configuration.set("merchants." + value.getName(), null);
    }

}
