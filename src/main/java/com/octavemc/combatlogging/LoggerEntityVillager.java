package com.octavemc.combatlogging;

import com.mojang.authlib.GameProfile;
import com.octavemc.Apollo;
import com.octavemc.combatlogging.event.LoggerDeathEvent;
import com.octavemc.combatlogging.event.LoggerRemovedEvent;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.LazyMetadataValue;

import java.util.Collections;
import java.util.UUID;
import java.util.logging.Level;

@Getter
public class LoggerEntityVillager extends EntityVillager implements Runnable {

    private final String name;
    private final UUID identifier;

    public LoggerEntityVillager(Player player, World world) {
        super(world);
        this.name = player.getName();
        this.identifier = player.getUniqueId();

        this.setPositionRotation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
        this.getBukkitEntity().setMetadata("combat-logger", new LazyMetadataValue(Apollo.getInstance(), () -> true));
        if (this.world.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)) {
            Apollo.getInstance().getServer().getLogger().log(Level.INFO, "Spawned a Combat Logger for " + player.getName());
        } else {
            Apollo.getInstance().getServer().getLogger().log(Level.SEVERE, "Failed to spawn Combat Logger for " + player.getName());
        }
    }

    @Override
    public void move(double d0, double d1, double d2) {}

    @Override
    public void collide(Entity entity) {}

    @Override
    public void makeSound(String s, float f, float f1) {}

    @Override
    public void die(DamageSource damagesource) {
        super.die(damagesource);
        var entity = new EntityPlayer(MinecraftServer.getServer(), (WorldServer) super.world, new GameProfile(identifier, name), new PlayerInteractManager(world));
        entity.a(MinecraftServer.getServer().getPlayerList().a(entity));
        Apollo.getInstance().getServer().getPluginManager().callEvent(new PlayerDeathEvent(entity.getBukkitEntity().getPlayer(), Collections.EMPTY_LIST, (int) entity.exp, ChatColor.GRAY + "(" + ChatColor.AQUA + "Combat Logger" + ChatColor.GRAY + ") " + this.combatTracker.b().c()));
        entity.inventory.n();
        entity.setHealth(0.0F);
        entity.dead = true;
        // MinecraftServer.getServer().getPlayerList().savePlayerFile(entity);
        Apollo.getInstance().getServer().getPluginManager().callEvent(new LoggerDeathEvent(this));
    }

    @Override
    public void run() {
        this.destroy();
    }

    public void destroy() {
        world.removeEntity(this);
        Bukkit.getPluginManager().callEvent(new LoggerRemovedEvent(this));
    }
}
