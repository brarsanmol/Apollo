package com.octavemc.economy.merchants;

import com.google.gson.GsonBuilder;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.util.InventoryUtils;
import com.octavemc.util.ItemBuilder;
import com.octavemc.util.NameUtils;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.LazyMetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@Getter
public class MerchantEntityVillager extends EntityVillager implements ConfigurationSerializable {

    // Priest = 2, Butcher = 4
    private static final int PROFESSION = 4;

    private String name;
    private MerchantType type;
    private Map<ItemStack, Double> lootTable;
    private Inventory inventory;

    public MerchantEntityVillager(World world) {
        super(world);

        this.getBukkitEntity().setMetadata("merchant", new LazyMetadataValue(Apollo.getInstance(), () -> true));
    }

    public MerchantEntityVillager(Location location, String name, MerchantType type) {
        super(((CraftWorld) location.getWorld()).getHandle());

        this.name = name;
        this.type = type;
        this.lootTable = new HashMap<>();
        this.setProfession(PROFESSION);
        this.setCustomName(ChatColor.AQUA + this.name + ChatColor.GRAY + " Merchant (" + ChatColor.AQUA + NameUtils.getPrettyName(type.name()) + ChatColor.GRAY + ')');
        this.setCustomNameVisible(true);
        this.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.getBukkitEntity().setMetadata("merchant", new LazyMetadataValue(Apollo.getInstance(), () -> true));
        if (this.world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)) {
            Apollo.getInstance().getServer().getLogger().log(Level.INFO, "Spawned The Merchant " + name);
        } else {
            Apollo.getInstance().getServer().getLogger().log(Level.SEVERE, "Failed To Spawn The Merchant " + name);
        }
    }

    public MerchantEntityVillager(Player player, String name, MerchantType type) {
        this(player.getLocation(), name, type);
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        return false;
    }

    @Override
    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        this.getBukkitEntity().setMetadata("merchant", new LazyMetadataValue(Apollo.getInstance(), () -> true));
        if (this.world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)) {
            Apollo.getInstance().getServer().getLogger().log(Level.INFO, "Spawned The Merchant " + name);
        } else {
            Apollo.getInstance().getServer().getLogger().log(Level.SEVERE, "Failed To Spawn The Merchant " + name);
        }
        //this.lootTable = (Map<ItemStack, Double>) new GsonBuilder().create().fromJson(String.valueOf(nbttagcompound.get("lootTable")), Map.class);
        System.out.println("HELLo!");
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        //nbttagcompound.set("lootTable", new NBTTagString(new GsonBuilder().create().toJson(this.lootTable)));
        super.b(nbttagcompound);
    }

    @Override
    public void move(double d0, double d1, double d2) {
        return;
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
    }

    @Override
    public void collide(Entity entity) {
        return;
    }

    @Override
    public void makeSound(String s, float f, float f1) {
        return;
    }

    public void put(ItemStack stack, double price) {
        this.lootTable.put(stack, price);
    }

    public Inventory getInventory() {
        if (this.inventory == null || !this.inventory.getContents().equals(this.lootTable.keySet().toArray(ItemStack[]::new))) {
            this.inventory = Apollo.getInstance().getServer().createInventory(null, InventoryUtils.getSafestInventorySize(this.lootTable.size()), name);
            this.inventory.setContents(this.lootTable.entrySet().stream().map(entry -> new ItemBuilder(entry.getKey()).loreLine(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Price: " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + entry.getValue()).build()).toArray(ItemStack[]::new));
        }
        return this.inventory;
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("name", this.name);
        map.put("type", this.type.name());
        map.put("lootTable", this.lootTable);
        map.put("location.world", this.world.getWorldData().getName());
        map.put("location.x", this.locX);
        map.put("location.y", this.locY);
        map.put("location.z", this.locZ);
        map.put("location.pitch", this.pitch);
        map.put("location.yaw", this.yaw);
        return map;
    }

    @Override
    public String toString() {
        return "MerchantEntityVillager{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", lootTable=" + lootTable +
                ", inventory=" + inventory +
                // ", world=" + world.getWorldData().getName() +
                ", locX=" + locX +
                ", locY=" + locY +
                ", locZ=" + locZ +
                '}';
    }
}
