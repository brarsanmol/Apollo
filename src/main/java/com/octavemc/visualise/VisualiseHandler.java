package com.octavemc.visualise;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.octavemc.util.cuboid.Cuboid;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.spigotmc.AsyncCatcher;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class VisualiseHandler {

    private final Table<UUID, Location, VisualBlock> storedVisualises = HashBasedTable.create();

    /**
     * Gets a {@link VisualBlock} for a {@link Player}.
     *
     * @param player   the {@link Player} to get for
     * @param location the {@link Location} to get at
     * @return the {@link VisualBlock} or none
     * @throws NullPointerException if player or location is null
     */
    public VisualBlock getVisualBlockAt(@NonNull Player player, @NonNull Location location) throws NullPointerException {
        return storedVisualises.get(player.getUniqueId(), location);
    }

    /**
     * Gets the current {@link VisualBlock}s to their {@link Location}s that are shown
     * to a {@link Player} of a specific {@link VisualType}.
     *
     * @param player the {@link Player} to get for
     * @return copied map of {@link VisualBlock}s shown to a {@link Player}.
     */
    public Map<Location, VisualBlock> getVisualBlocks(Player player) {
        return new HashMap<>(storedVisualises.row(player.getUniqueId()));
    }

    /**
     * Gets the current {@link VisualBlock}s to their {@link Location}s that are shown
     * to a {@link Player} of a specific {@link VisualType}.
     *
     * @param player     the {@link Player} to get for
     * @param visualType the {@link VisualType} to get for
     * @return copied map of {@link VisualBlock}s shown to a {@link Player}.
     */
    public Map<Location, VisualBlock> getVisualBlocks(Player player, VisualType visualType) {
        return Maps.filterValues(getVisualBlocks(player), visualBlock -> visualType == visualBlock.getVisualType());
    }

    public LinkedHashMap<Location, VisualBlockData> generate(Player player, Cuboid cuboid, VisualType visualType, boolean canOverwrite) {
        Collection<Location> locations = new HashSet<>(cuboid.getSizeX() * cuboid.getSizeY() * cuboid.getSizeZ());
        Iterator<Location> iterator = cuboid.locationIterator();
        while (iterator.hasNext()) {
            locations.add(iterator.next());
        }

        return generate(player, locations, visualType, canOverwrite);
    }

    @SneakyThrows
    public LinkedHashMap<Location, VisualBlockData> generate(Player player, Iterable<Location> locations, VisualType visualType, boolean canOverwrite) {
        LinkedHashMap<Location, VisualBlockData> results = new LinkedHashMap<>();
        ArrayList<VisualBlockData> filled = visualType.blockFiller().bulkGenerate(player, locations);
        if (filled != null) {
            int count = 0;
            Map<Location, MaterialData> updatedBlocks = new HashMap<>();
            for (Location location : locations) {
                if (!canOverwrite && storedVisualises.contains(player.getUniqueId(), location)) continue;
                if (location.getBlock().getType().isSolid() || location.getBlock().getType() != Material.AIR) continue;

                var visualBlockData = filled.get(count++);
                results.put(location, visualBlockData);
                updatedBlocks.put(location, visualBlockData);
                storedVisualises.put(player.getUniqueId(), location, new VisualBlock(visualType, visualBlockData, location));
            }
            VisualiseUtil.handleBlockChanges(player, updatedBlocks);
        }

        return results;
    }

    /**
     * Clears a visual block at a given location for a player.
     *
     * @param player   the player to clear for
     * @param location the location to clear at
     * @return if the visual block was shown in the first place
     */
    public void clearVisualBlock(Player player, Location location) {
        clearVisualBlock(player, location, true);
    }

    /**
     * Clears a visual block at a given location for a player.
     *
     * @param player            the player to clear for
     * @param location          the location to clear at
     * @param sendRemovalPacket if a packet to send a block change should be sent
     *                          (this is used to prevent unnecessary packets sent when
     *                          disconnecting or changing worlds, for example)
     * @return if the visual block was shown in the first place
     */
    public void clearVisualBlock(Player player, Location location, boolean sendRemovalPacket) {
        var block = storedVisualises.remove(player.getUniqueId(), location);
        if (sendRemovalPacket
                && block != null
                && block.getBlockData().getBlockType() != location.getBlock().getType()
                || block.getBlockData().getData() != location.getBlock().getData()) {
            player.sendBlockChange(location, location.getBlock().getType(), location.getBlock().getData());
        }
    }

    /**
     * Clears all visual blocks in a {@link Chunk}.
     *
     * @param chunk the {@link Chunk} to clear in
     */
    public void clearVisualBlocks(Chunk chunk) {
        AsyncCatcher.catchOp("Chunk operation");
        if (!storedVisualises.isEmpty()) {
            Set<Location> keys = storedVisualises.columnKeySet();
            for (Location location : new HashSet<>(keys)) {
                if (location.getWorld().equals(chunk.getWorld()) && chunk.getX() == (((int) location.getX()) >> 4) && chunk.getZ() == (((int) location.getZ()) >> 4)) {
                    keys.remove(location);
                }
            }
        }
    }

    /**
     * Clears all visual blocks that are shown to a player.
     *
     * @param player the player to clear for
     */
    public void clearVisualBlocks(Player player) {
        clearVisualBlocks(player, null, null);
    }

    /**
     * Clears all visual blocks that are shown to a player of a given VisualType.
     *
     * @param player     the player to clear for
     * @param visualType the visual type
     * @param predicate  the predicate to filter to
     */
    public void clearVisualBlocks(Player player, @Nullable VisualType visualType, @Nullable Predicate<VisualBlock> predicate) {
        clearVisualBlocks(player, visualType, predicate, true);
    }

    /**
     * Clears all visual blocks that are shown to a player of a given VisualType.
     *
     * @param player             the player to clear for
     * @param visualType         the visual type
     * @param predicate          the predicate to filter to
     * @param sendRemovalPackets if a packet to send a block change should be sent
     *                           (this is used to prevent unnecessary packets sent when
     *                           disconnecting or changing worlds, for example)
     */
    @Deprecated
    public void clearVisualBlocks(Player player,
                                  @Nullable VisualType visualType,
                                  @Nullable Predicate<VisualBlock> predicate,
                                  boolean sendRemovalPackets) {
        if (!storedVisualises.containsRow(player.getUniqueId())) return;
        Map<Location, VisualBlock> results = new HashMap<>(storedVisualises.row(player.getUniqueId())); // copy to prevent commodification
        Map<Location, VisualBlock> removed = new HashMap<>();
        results.entrySet().stream()
                .filter(entry -> (predicate == null
                        || predicate.test(entry.getValue()))
                        && (visualType == null
                        || entry.getValue().getVisualType() == visualType)
                        && removed.put(entry.getKey(), entry.getValue()) == null)
                .forEach(entry -> clearVisualBlock(player, entry.getKey(), sendRemovalPackets));
    }
}
