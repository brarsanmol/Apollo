package com.octavemc.crates;

import com.octavemc.Apollo;
import com.octavemc.util.InventoryUtils;
import com.octavemc.util.ItemBuilder;
import com.octavemc.util.PersistableLocation;
import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class Crate implements ConfigurationSerializable {

    private String identifier;
    private PersistableLocation location;
    private ItemStack key;
    private double total;
    private TreeMap<Double, ItemStack> lootTable;
    private Inventory inventory;

    public Crate(String identifier, Material material, Location location) {
        this.identifier = identifier;
        this.location = new PersistableLocation(location);
        this.key = new ItemBuilder(material)
                .amount(1)
                .displayName(ChatColor.AQUA + identifier + " Key")
                .loreLine(ChatColor.WHITE + "A crate key for " + ChatColor.AQUA + identifier)
                .loreLine(ChatColor.WHITE + "Go to spawn to and right click on the " + ChatColor.AQUA + identifier + " crate to redeem it!")
                .build();
        this.lootTable = new TreeMap<>();
    }

    public Crate(Map<String, Object> map) {
        this.identifier = (String) map.get("identifier");
        this.key = (ItemStack) map.get("key");
        this.location = (PersistableLocation) map.get("location");
        this.lootTable = new TreeMap<>((LinkedHashMap<Double, ItemStack>) map.get("lootTable"));
    }

    public ItemStack put(Double weight, ItemStack value) {
        this.total += weight;
        return this.lootTable.put(this.total, new ItemBuilder(value)
                .lore(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.AQUA + "" + ChatColor.BOLD + "!" + ChatColor.DARK_GRAY + "" + ChatColor.BOLD + ") " + ChatColor.GRAY +  "Drop Chance: " + ChatColor.AQUA + weight + "%")
                .build());
    }

    public ItemStack next() {
        return this.lootTable.ceilingEntry(ThreadLocalRandom.current().nextDouble() * total).getValue();
    }

    public Inventory getInventory() {
        if (this.inventory == null || !this.inventory.getContents().equals(this.lootTable.values().toArray(ItemStack[]::new))) {
            this.inventory = Apollo.getInstance().getServer().createInventory(null, InventoryUtils.getSafestInventorySize(this.lootTable.size()), identifier);
            this.inventory.setContents(this.lootTable.values().toArray(ItemStack[]::new));
        }
        return this.inventory;
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("identifier", identifier);
        map.put("location", location);
        map.put("key", key);
        map.put("lootTable", lootTable);
        return map;
    }
}
