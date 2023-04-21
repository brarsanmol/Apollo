package com.octavemc.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

/**
 * Library used for displaying {@link ParticleEffect}s.
 * <p>http://wiki.vg/Protocol</p>
 */
@UtilityClass
public class ParticleEffect {

    /**
     * Displays this {@link ParticleEffect} to a {@link Player}
     *
     * @param player the {@link Player} to show
     * @param x      the x co-ordinate to show at
     * @param y      the y co-ordinate to show at
     * @param z      the z co-ordinate to show at
     * @param speed  the speed (or color depending on the effect)
     * @param amount the amount to show
     */
    public static void display(EnumParticle particle, Player player, float x, float y, float z, float speed, int amount) {
        display(particle, player, x, y, z, 0.0F, 0.0F, 0.0F, speed, amount);
    }

    /**
     * Displays this {@link ParticleEffect} to a {@link Player}
     *
     * @param player  the {@link Player} to show
     * @param x       the x co-ordinate to show at
     * @param y       the y co-ordinate to show at
     * @param z       the z co-ordinate to show at
     * @param offsetX the x range of the particle effect
     * @param offsetY the y range of the particle effect
     * @param offsetZ the z range of the particle effect
     * @param speed   the speed (or color depending on the effect)
     * @param amount  the amount of particles to show
     */
    public static void display(EnumParticle particle, Player player, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        PacketPlayOutWorldParticles packet = createPacket(particle, x, y, z, offsetX, offsetY, offsetZ, speed, amount);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Displays this {@link ParticleEffect} to a {@link Player}
     *
     * @param player   the {@link Player} to show
     * @param location the {@link Location} to show at
     * @param speed    the speed (or color depending on the effect)
     * @param amount   the amount of particles to show
     */
    public static void display(EnumParticle particle, Player player, Location location, float speed, int amount) {
        display(particle, player, location, 0.0F, 0.0F, 0.0F, speed, amount);
    }

    /**
     * Displays this {@link ParticleEffect} to a {@link Player}
     *
     * @param player   the {@link Player} to show
     * @param location the {@link Location} to show at
     * @param offsetX  the x range of the particle effect
     * @param offsetY  the y range of the particle effect
     * @param offsetZ  the z range of the particle effect
     * @param speed    the speed (or color depending on the effect)
     * @param amount   the amount of particles to show
     */
    public static void display(EnumParticle particle, Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        Packet packet = createPacket(particle, location, offsetX, offsetY, offsetZ, speed, amount);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Send this {@link ParticleEffect} to all online {@link Player}s
     *
     * @param x       the x co-ordinate to show at
     * @param y       the y co-ordinate to show at
     * @param z       the z co-ordinate to show at
     * @param offsetX the x range of the particle effect
     * @param offsetY the y range of the particle effect
     * @param offsetZ the z range of the particle effect
     * @param speed   the speed (or color depending on the effect)
     * @param amount  the amount of particles to show
     */
    public static void broadcast(EnumParticle particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        Packet packet = createPacket(particle, x, y, z, offsetX, offsetY, offsetZ, speed, amount);
        for (Player player : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    /**
     * Send this {@link ParticleEffect} to all online {@link Player}s
     *
     * @param location the {@link Location} to show at
     * @param offsetX  the x range of the particle effect
     * @param offsetY  the y range of the particle effect
     * @param offsetZ  the z range of the particle effect
     * @param speed    the speed (or color depending on the effect)
     * @param amount   the amount of particles to show
     */
    public static void broadcast(EnumParticle particle, Location location, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        broadcast(particle, location, offsetX, offsetY, offsetZ, speed, amount, null, null);
    }

    /**
     * Send this {@link ParticleEffect} to all online {@link Player}s
     *
     * @param location the {@link Location} to show at
     * @param offsetX  the x range of the particle effect
     * @param offsetY  the y range of the particle effect
     * @param offsetZ  the z range of the particle effect
     * @param speed    the speed (or color depending on the effect)
     * @param amount   the amount of particles to show
     * @param source   the source of this {@link ParticleEffect} or null
     */
    public static void broadcast(EnumParticle particle, Location location, float offsetX, float offsetY, float offsetZ, float speed, int amount, @Nullable Entity source) {
        broadcast(particle, location, offsetX, offsetY, offsetZ, speed, amount, source, null);
    }

    /**
     * Send this {@link ParticleEffect} to all online {@link Player}s
     *
     * @param location  the {@link Location} to show at
     * @param offsetX   the x range of the particle effect
     * @param offsetY   the y range of the particle effect
     * @param offsetZ   the z range of the particle effect
     * @param speed     the speed (or color depending on the effect)
     * @param amount    the amount of particles to show
     * @param source    the source of this {@link ParticleEffect} or null
     * @param predicate the predicate to apply to determine if the player should see it
     */
    public static void broadcast(EnumParticle particle, Location location, float offsetX, float offsetY, float offsetZ, float speed, int amount,
                          @Nullable Entity source, @Nullable Predicate<Player> predicate) {

        Packet packet = createPacket(particle, location, offsetX, offsetY, offsetZ, speed, amount);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if ((source == null || (source instanceof Player && player.canSee((Player) source)))
                    && (predicate == null || predicate.apply(player))) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    /**
     * Displays this {@link ParticleEffect} as a sphere to a {@link Player}
     *
     * @param player   the {@link Player} to show
     * @param location the {@link Location} of the center
     * @param radius   the radius of the sphere
     */
    public static void sphere(EnumParticle particle, @Nullable Player player, Location location, float radius) {
        sphere(particle, player, location, radius, 20f, 2);
    }

    /**
     * Displays this {@link ParticleEffect} as a sphere to a {@link Player} or to all online
     *
     * @param player    the {@link Player} to show or null to broadcast to every player
     * @param location  the {@link Location} of the center
     * @param radius    the radius of the sphere
     * @param density   the density of particle locations
     * @param intensity the number of particles at each location
     */
    public static void sphere(EnumParticle particle, @Nullable Player player, Location location, float radius, float density, int intensity) {
        Preconditions.checkNotNull(location, "Location cannot be null");
        Preconditions.checkArgument(radius >= 0, "Radius must be positive");
        Preconditions.checkArgument(density >= 0, "Density must be positive");
        Preconditions.checkArgument(intensity >= 0, "Intensity must be positive");

        float deltaPitch = 180 / density;
        float deltaYaw = 360 / density;
        World world = location.getWorld();
        for (int i = 0; i < density; i++) {
            for (int j = 0; j < density; j++) {
                float pitch = -90 + (j * deltaPitch);
                float yaw = -180 + (i * deltaYaw);
                float x = radius * MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI) * -MathHelper.cos(-pitch * 0.017453292F) + (float) location.getX();
                float y = radius * MathHelper.sin(-pitch * 0.017453292F) + (float) location.getY();
                float z = radius * MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI) * -MathHelper.cos(-pitch * 0.017453292F) + (float) location.getZ();

                Location target = new Location(world, x, y, z);
                if (player == null) {
                    broadcast(particle, target, 0f, 0f, 0f, 0f, intensity);
                } else {
                    display(particle, player, target, 0f, 0f, 0f, 0f, intensity);
                }
            }
        }
    }

    private static PacketPlayOutWorldParticles createPacket(EnumParticle particle, Location location, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        return createPacket(particle, (float) location.getX(), (float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, amount);
    }

    private static PacketPlayOutWorldParticles createPacket(EnumParticle particle, float x, float y, float z, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        Preconditions.checkArgument(speed >= 0, "Speed must be positive");
        Preconditions.checkArgument(amount > 0, "Cannot use less than one particle.");
        return new PacketPlayOutWorldParticles(particle, true, x, y, z, offsetX, offsetY, offsetZ, speed, amount);
    }
}