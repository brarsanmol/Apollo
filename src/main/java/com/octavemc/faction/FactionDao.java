package com.octavemc.faction;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.eventgame.faction.ConquestFaction;
import com.octavemc.eventgame.faction.EventFaction;
import com.octavemc.eventgame.faction.KothFaction;
import com.octavemc.faction.claim.Claim;
import com.octavemc.faction.event.FactionClaimChangedEvent;
import com.octavemc.faction.event.FactionCreateEvent;
import com.octavemc.faction.event.FactionRemoveEvent;
import com.octavemc.faction.event.cause.ClaimChangeCause;
import com.octavemc.faction.struct.ChatChannel;
import com.octavemc.faction.struct.Role;
import com.octavemc.faction.type.*;
import com.octavemc.util.Dao;
import dev.morphia.Datastore;
import dev.morphia.query.experimental.filters.Filters;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

//TODO: Figure out how to remove claim's not in use for a long time...
@Getter
public final class FactionDao extends BukkitRunnable implements Dao<ObjectId, Faction> {

    private final Datastore datastore;
    private final Map<ObjectId, Faction> cache;
    private final Map<UUID, ObjectId> playerFactionCache;
    private final Map<String, ObjectId> nameFactionCache;
    private final Table<String, Long, Claim> claimPositionMap;

    //TODO: Find a better way to implement this...
    private final WarzoneFaction warzone;
    private final WildernessFaction wilderness;
    private final SpawnFaction spawn;
    private final RoadFaction.NorthRoadFaction north;
    private final RoadFaction.EastRoadFaction east;
    private final RoadFaction.SouthRoadFaction south;
    private final RoadFaction.WestRoadFaction west;

    public FactionDao(Datastore datastore) {
        this.datastore = datastore;
        this.cache = new ConcurrentHashMap<>();
        this.playerFactionCache = new HashMap<>();
        this.nameFactionCache = new HashMap<>();
        this.claimPositionMap = HashBasedTable.create();

        this.warzone = new WarzoneFaction();
        this.wilderness = new WildernessFaction();
        this.spawn = new SpawnFaction();
        this.north = new RoadFaction.NorthRoadFaction();
        this.east = new RoadFaction.EastRoadFaction();
        this.south = new RoadFaction.SouthRoadFaction();
        this.west = new RoadFaction.WestRoadFaction();

        this.indexFaction(this.spawn.getIdentifier(), this.spawn);
        this.indexFaction(this.north.getIdentifier(), this.north);
        this.indexFaction(this.east.getIdentifier(), this.east);
        this.indexFaction(this.west.getIdentifier(), this.west);
        this.indexFaction(this.south.getIdentifier(), this.south);

        this.runTaskTimerAsynchronously(Apollo.getInstance(), 20L * 300, 20L * 300);
    }

    /**
     * Return's a {@link Faction} from it's {@link ObjectId} identifier
     * @param identifier the faction identifier
     * @return The {@link Faction}
     */
    @Override
    public Optional<Faction> get(@NonNull ObjectId identifier) {
        return this.cache.containsKey(identifier)
                ? Optional.of(this.cache.get(identifier))
                : Optional.empty();
    }

    /**
     * Get a specific {@link Faction} from it's {@link String} name.
     * @param name the faction display name
     * @return The {@link Faction}
     */
    public Optional<Faction> getByName(@NonNull String name) {
        return this.nameFactionCache.containsKey(name)
                ? Optional.of(this.cache.get(this.nameFactionCache.get(name)))
                : Optional.empty();
    }

    /**
     * Get a {@link PlayerFaction} from a {@link FactionMember}'s {@link UUID}
     * @param identifier the {@link FactionMember}'s {@link UUID}
     * @return The {@link PlayerFaction} for the {@link FactionMember}
     */
    @SneakyThrows
    public Optional<PlayerFaction> getByPlayer(@NonNull UUID identifier) {
        return this.playerFactionCache.containsKey(identifier)
                ? Optional.of((PlayerFaction) this.cache.get(this.playerFactionCache.get(identifier)))
                : Optional.empty();
    }

    /**
     * Get's a {@link Faction} by either searching
     * @param search the {@link Faction} display name or a {@link FactionMember}'s in game name.
     * @return The {@link Faction} or {@link PlayerFaction}
     */
    public Optional<? extends Faction> getContaining(@NonNull String search) {
        var faction = this.getByName(search);
        return faction.isPresent()
                ? faction
                : this.getByPlayer(Apollo.getInstance().getServer().getOfflinePlayer(search).getUniqueId());
    }

    /**
     * Get's the {@link Claim} at the coordinates and world provided.
     * @param world the minecraft world {@link World}
     * @param x the x coordinate
     * @param z the z coordinate
     * @return The {@link Claim} at {@link World}, x, and z
     */
    public Optional<Claim> getClaimAt(@NonNull World world, int x, int z) {
        var claim = this.claimPositionMap.get(world.getName(), LongHash.toLong(x, z));
        return claim == null ? Optional.empty() : Optional.of(claim);
    }

    /**
     * Get's the {@link Claim} at the {@link Location} provided.
     * @param location the {@link Location} of a block, player, entity, etc...
     * @return The {@link Claim} at {@link Location}
     */
    public Optional<Claim> getClaimAt(@NonNull Location location) {
        return this.getClaimAt(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    /**
     * Get's the {@link Faction} at a {@link Claim} using the {@link World} and x, z coordinates.
     * @param world the minecraft world {@link World}
     * @param x the x coordinate
     * @param z the z coordinate
     * @return The {@link Faction} at {@link World}, x, and z
     */
    public Optional<Faction> getFactionAt(World world, int x, int z) {
        var claim = this.getClaimAt(world, x, z);
        if (world.getEnvironment() == World.Environment.THE_END) {
            return Optional.of(this.warzone);
        } else if (claim.isPresent() && claim.get().getFaction() != null) {
            return Optional.of(claim.get().getFaction());
        }
        return Math.abs(x) > Configuration.WARZONE_RADIUS.get(world.getEnvironment())
                || Math.abs(z) > Configuration.WARZONE_RADIUS.get(world.getEnvironment()) ? Optional.of(this.wilderness) : Optional.of(this.warzone);
    }

    /**
     * Get's the {@link Faction} at a {@link Claim} using a {@link Location}.
     * @param location the {@link Location} of a block, player, entity, etc...
     * @return The {@link Faction} at {@link Location}
     */
    public Optional<Faction> getFactionAt(@NonNull Location location) {
        return this.getFactionAt(location.getWorld(), location.getBlockX(), location.getBlockZ());
    }

    /**
     * Get's all {@link Faction}'s from the Mongo datastore.
     * @return all the factions in the Mongo datastore.
     */
    @SneakyThrows
    @Override
    public List<Faction> getAll() {
        return CompletableFuture.supplyAsync(() -> this.datastore.find(Faction.class).iterator().toList()).get();
    }

    /**
     * Get's all @{link Faction}'s by a Type from the Mongo datastore.
     * @param value
     * @return
     */
    @SneakyThrows
    public List<? extends Faction> getAll(Class<? extends Faction> value) {
        return CompletableFuture.supplyAsync(() -> this.datastore.find(value).iterator().toList()).get();
    }

    @SneakyThrows
    public List<PlayerFaction> getAllPlayerFactions() {
        return CompletableFuture.supplyAsync(() -> this.datastore.find(PlayerFaction.class).iterator().toList()).get();
    }

    @SneakyThrows
    public List<EventFaction> getAllEventFactions() {
        return CompletableFuture.supplyAsync(() -> this.datastore.find(EventFaction.class).iterator().toList()).get();
    }

    /**
     * Loads all {@link Faction}'s from the Mongo Datastore.
     * This operation should be run async, other than the initial load.
     */
    @Override
    public void loadAll() {
        this.getAll(PlayerFaction.class).forEach(faction -> {
            if (((PlayerFaction) faction).getMembers().isEmpty()) {
                this.deleteByType(PlayerFaction.class, faction);
                return;
            }
            this.indexFaction(faction.getIdentifier(), faction);
        });
        this.getAll(KothFaction.class).forEach(faction -> this.indexFaction(faction.getIdentifier(), faction));
        this.getAll(ConquestFaction.class).forEach(faction -> this.indexFaction(faction.getIdentifier(), faction));
    }

    /**
     * Updates a {@link Faction} to the Mongo Datastore.
     * This operation should be run async.
     * @param faction the faction
     */
    @Override
    public void update(Faction faction) {
        this.datastore.merge(faction);
    }

    /**
     * Updates all {@link Faction}'s from the Mongo Datastore.
     * This operation should be run async, other than the final save.
     */
    @Override
    public void updateAll() {
        this.cache.values().forEach(this::update);
    }

    /**
     * Saves a {@link Faction} to the Mongo Datastore.
     * This operation should be run async.
     * @param faction the faction
     */
    @Override
    public void save(Faction faction) {
        this.datastore.save(faction);
    }

    /**
     * Saves all {@link Faction}'s from the Mongo Datastore.
     * This operation should be run async, other than the final save.
     */
    @Override
    public void saveAll() {
        this.cache.values().stream()
                .filter(faction -> faction instanceof EventFaction || faction instanceof PlayerFaction)
                .forEach(this::save);
    }

    /**
     * Deletes a {@link Faction}'s by a Type from the Mongo datastore.
     * @param value
     * @return
     */
    @SneakyThrows
    public void deleteByType(Class<? extends Faction> value, Faction faction) {
        this.datastore
                .find(value)
                .filter(Filters.eq("_id", faction.getIdentifier())).delete();
    }

    /**
     * Create's a {@link Faction} for a {@link CommandSender}.
     * @param faction the faction to be created
     * @param sender the creator of the faction
     * @return Whether the faction was created successfully.
     */
    public boolean create(@NonNull Faction faction, @NonNull CommandSender sender) {
        if (faction instanceof PlayerFaction provided && sender instanceof Player player) {
            var event = new FactionCreateEvent(faction, sender);
            Apollo.getInstance().getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
            if (!provided.addMember(sender, player, player.getUniqueId(), new FactionMember(player.getUniqueId(), ChatChannel.PUBLIC, Role.LEADER))) return false;
        }
        this.indexFaction(faction.getIdentifier(), faction);
        return true;
    }

    /**
     * Remove a {@link Faction}.
     * @param faction the faction to be remove
     * @param sender the player who send the removal
     * @return Whether the faction was removed successfully.
     */
    public boolean remove(@NonNull Faction faction, @NonNull CommandSender sender) {
        if (!this.cache.containsKey(faction.getIdentifier())) return false;
        this.nameFactionCache.remove(faction.getName());
        this.cache.remove(faction.getIdentifier());
        var event = new FactionRemoveEvent(faction, sender);
        Apollo.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;
        if (faction instanceof ClaimableFaction) Apollo.getInstance().getServer().getPluginManager().callEvent(new FactionClaimChangedEvent(sender, ClaimChangeCause.UNCLAIM, ((ClaimableFaction) faction).getClaims()));

        if (faction instanceof PlayerFaction target) {
            target.getAlliedFactions().forEach(ally -> ally.getRelations().remove(faction.getIdentifier()));
            for (Iterator<FactionMember> members = target.getMembers().iterator(); members.hasNext();) {
                var member = members.next();
                this.playerFactionCache.remove(member.getUniqueID(), faction.getIdentifier());
                target.removeMember(sender, null, member.getUniqueID(),true, true);
            }
        }
        CompletableFuture.runAsync(() -> this.deleteByType((faction instanceof PlayerFaction
                ? PlayerFaction.class
                : (faction instanceof KothFaction ? KothFaction.class : ConquestFaction.class)), faction));
        return true;
    }

    /**
     * Put a {@link Faction} in the cache.
     * @param identifier the {@link Faction} identifier
     * @param faction the {@link Faction}
     */
    public boolean indexFaction(@NonNull ObjectId identifier, @NonNull Faction faction) {
        if (this.cache.putIfAbsent(identifier, faction) != null || this.nameFactionCache.putIfAbsent(faction.getName(), identifier) != null) return false;

        if (faction instanceof ClaimableFaction) {
            ((ClaimableFaction) faction).getClaims().forEach(claim -> this.indexClaim(claim, ClaimChangeCause.CLAIM));
        }

        if (faction instanceof PlayerFaction) {
            ((PlayerFaction) faction).getMembers().forEach(member -> {
                if (this.playerFactionCache.put(member.getUniqueID(), identifier) != null) return;
            });
        }
        return true;
    }

    /**
     * Manages the indexing of {@link Claim}'s in the claim position table.
     * @param claim the {@link Claim} to be changed
     * @param cause the cause of the {@link Claim} change
     */
    public void indexClaim(@NonNull Claim claim, @NonNull ClaimChangeCause cause) {
        IntStream.range(Math.min(claim.getX1(), claim.getX2()), Math.max(claim.getX1(), claim.getX2())).forEach(x ->
            IntStream.range(Math.min(claim.getZ1(), claim.getZ2()), Math.max(claim.getZ1(), claim.getZ2())).forEach(z -> {
                switch (cause) {
                    case CLAIM -> this.claimPositionMap.put(claim.getWorldName(), LongHash.toLong(x, z), claim);
                    case UNCLAIM -> this.claimPositionMap.remove(claim.getWorldName(), LongHash.toLong(x, z));
                }
            })
        );
    }

    /**
     * Save all {@link Faction}'s that are loaded, and remove any cached {@link PlayerFaction}'s that do not have members online.
     */
    @Override
    public void run() {
        this.saveAll();
    }
}
