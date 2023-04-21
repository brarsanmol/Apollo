package com.octavemc.faction.type;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.DateTimeFormats;
import com.octavemc.faction.FactionMember;
import com.octavemc.faction.event.*;
import com.octavemc.faction.event.cause.FactionLeaveCause;
import com.octavemc.faction.struct.Raidable;
import com.octavemc.faction.struct.RegenStatus;
import com.octavemc.faction.struct.Relation;
import com.octavemc.faction.struct.Role;
import com.octavemc.timer.type.TeleportTimer;
import com.octavemc.user.User;
import com.octavemc.util.BukkitUtils;
import com.octavemc.util.PersistableLocation;
import dev.morphia.annotations.Property;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
public class PlayerFaction extends ClaimableFaction implements Raidable {

    // The UUID is the Faction unique ID.
    @Property
    protected final Map<ObjectId, Relation> requestedRelations = new HashMap<>();
    @Property
    protected final Map<ObjectId, Relation> relations = new HashMap<>();
    @Property
    protected final Set<String> invitedPlayerNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER); // we are storing names as offline lookups are slow
    @Property
    protected final Set<FactionMember> members = new TreeSet<>(Comparator.comparingInt(o -> o.getRole().ordinal()));

    @Property
    protected PersistableLocation home;
    @Property
    protected String announcement;
    @Property
    protected int balance;
    @Property
    protected double deathsUntilRaidable = 1.0D;
    @Property
    protected long regenCooldownTimestamp;

    public PlayerFaction(String name) {
        super(name);
    }

    public boolean addMember(CommandSender sender, @Nullable Player player, UUID playerUUID, FactionMember factionMember) {
        for (FactionMember member : this.members) {
            if (member.getUniqueID().equals(playerUUID)) {
                return false;
            }
        }
        PlayerJoinFactionEvent eventPre = new PlayerJoinFactionEvent(sender, player, playerUUID, this);
        Bukkit.getPluginManager().callEvent(eventPre);
        if (eventPre.isCancelled()) {
            return false;
        }
        lastDtrUpdateTimestamp = System.currentTimeMillis();
        invitedPlayerNames.remove(factionMember.getName());
        this.members.add(factionMember);
        Bukkit.getPluginManager().callEvent(new PlayerJoinedFactionEvent(sender, player, playerUUID, this));
        return true;
    }

    public boolean removeMember(CommandSender sender, @Nullable Player player, UUID playerUUID, boolean kick, boolean force) {
        Iterator<FactionMember> iterator = this.members.iterator();
        while (iterator.hasNext()) {
            FactionMember member = iterator.next();
            if (!member.getUniqueID().equals(playerUUID)) {
                continue;
            } else {
                // Call pre event.
                PlayerLeaveFactionEvent preEvent = new PlayerLeaveFactionEvent(sender, player, playerUUID, this, FactionLeaveCause.LEAVE, kick, force);
                Bukkit.getPluginManager().callEvent(preEvent);
                if (preEvent.isCancelled()) {
                    return false;
                }

                this.setDeathsUntilRaidable(Math.min(this.deathsUntilRaidable, this.getMaximumDeathsUntilRaidable()));
                if (force) {
                    this.setDeathsUntilRaidable(getDeathsUntilRaidable() - 1);
                }

                // Call after event.
                PlayerLeftFactionEvent event = new PlayerLeftFactionEvent(sender, player, playerUUID, this, FactionLeaveCause.LEAVE, kick, false);
                Bukkit.getPluginManager().callEvent(event);
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a list of faction UUIDs that are allied to this {@link PlayerFaction}.
     *
     * @return mutable list of UUIDs
     */
    public Collection<ObjectId> getAllied() {
        return Maps.filterValues(relations, relation -> relation == Relation.ALLY).keySet();
    }

    /**
     * Gets a list of {@link PlayerFaction}s that are allied to this {@link PlayerFaction}.
     *
     * @return mutable list of {@link PlayerFaction}s
     */
    public List<PlayerFaction> getAlliedFactions() {
        Collection<ObjectId> allied = getAllied();
        Iterator<ObjectId> iterator = allied.iterator();
        List<PlayerFaction> results = new ArrayList<>(allied.size());
        while (iterator.hasNext()) {
            Apollo.getInstance().getFactionDao().get(iterator.next()).filter(faction -> faction instanceof PlayerFaction).ifPresentOrElse(faction -> results.add((PlayerFaction) faction), () -> iterator.remove());
        }

        return results;
    }

    public Map<ObjectId, Relation> getRequestedRelations() {
        return requestedRelations;
    }

    public Map<ObjectId, Relation> getRelations() {
        return relations;
    }

    /**
     * Gets the members in this {@link PlayerFaction}.
     * <p>The key is the {@link UUID} of the member</p>
     * <p>The value is the {@link FactionMember}</p>
     *
     * @return map of members.
     */
    public Set<FactionMember> getMembers() {
        return this.members;
    }

    /**
     * Gets the online {@link Player}s in this {@link Faction}.
     *
     * @return set of online {@link Player}s
     */
    public Set<Player> getOnlinePlayers() {
        return getOnlinePlayers(null);
    }

    /**
     * Gets the online {@link Player}s in this {@link Faction} that are
     * visible to a {@link CommandSender}.
     *
     * @param sender the {@link CommandSender} to get for
     * @return a set of online players visible to sender
     */
    public Set<Player> getOnlinePlayers(CommandSender sender) {
        Set<Map.Entry<UUID, FactionMember>> entrySet = getOnlineMembers(sender).entrySet();
        Set<Player> results = new HashSet<>(entrySet.size());
        for (Map.Entry<UUID, FactionMember> entry : entrySet) {
            results.add(Bukkit.getPlayer(entry.getKey()));
        }

        return results;
    }

    /**
     * Gets the online members in this {@link Faction}.
     * <p>The key is the {@link UUID} of the member</p>
     * <p>The value is the {@link FactionMember}</p>
     *
     * @return an immutable set of online members
     */
    public Map<UUID, FactionMember> getOnlineMembers() {
        return getOnlineMembers(null);
    }

    /**
     * Gets the online members in this {@link Faction} that are visible to a {@link CommandSender}.
     * <p>The key is the {@link UUID} of the member</p>
     * <p>The value is the {@link FactionMember}</p>
     *
     * @param sender the {@link CommandSender} to get for
     * @return a set of online members visible to sender
     */
    public Map<UUID, FactionMember> getOnlineMembers(CommandSender sender) {
        Player senderPlayer = sender instanceof Player ? ((Player) sender) : null;
        Map<UUID, FactionMember> results = new HashMap<>();
        for (FactionMember member : this.members) {
            Player target = Bukkit.getPlayer(member.getUniqueID());
            if (target == null || (senderPlayer != null && !senderPlayer.canSee(target))) {
                continue;
            }

            results.put(member.getUniqueID(), member);
        }

        return results;
    }

    /**
     * Gets the leading {@link FactionMember} of this {@link Faction}.
     *
     * @return the leading {@link FactionMember}
     */
    public FactionMember getLeader() {
        for (FactionMember member : this.members) {
            if (member.getRole() == Role.LEADER) {
                return member;
            }
        }
        return null;
    }

    /**
     * Gets the {@link FactionMember} with a specific name.
     *
     * @param memberName the id to search for
     * @return the {@link FactionMember} or null if is not a member
     * @deprecated uses hanging offline player method
     */
    @Deprecated
    public FactionMember getMember(String memberName) {
        UUID uuid = Bukkit.getOfflinePlayer(memberName).getUniqueId(); //TODO: breaking
        return uuid == null ? null : getMember(uuid);
    }


    /**
     * Gets the {@link FactionMember} of a {@link Player}.
     *
     * @param player the {@link Player} to get for
     * @return the {@link FactionMember} or null if is not a member
     */
    public FactionMember getMember(Player player) {
        return this.getMember(player.getUniqueId());
    }

    /**
     * Gets the {@link FactionMember} with a specific {@link UUID}.
     *
     * @param memberUUID the {@link UUID} to get for
     * @return the {@link FactionMember} or null if is not a member
     */
    public FactionMember getMember(UUID memberUUID) {
        for (FactionMember member : this.members) {
            if (member.getUniqueID().equals(memberUUID)) {
                return member;
            }
        }
        return null;
    }

    /**
     * Gets the names of the players that have been
     * invited to join this {@link PlayerFaction}.
     *
     * @return set of invited player names
     */
    public Set<String> getInvitedPlayerNames() {
        return invitedPlayerNames;
    }

    public Location getHome() {
        return home == null ? null : home.getLocation();
    }

    public boolean hasHome() {
        return home != null;
    }

    public void setHome(@Nullable Location home) {
        if (home == null && this.home != null) {
            TeleportTimer timer = Apollo.getInstance().getTimerManager().getTeleportTimer();
            for (Player player : getOnlinePlayers()) {
                Location destination = timer.getDestination(player);
                if (Objects.equals(destination, this.home.getLocation())) {
                    timer.clearCooldown(player);
                    player.sendMessage(ChatColor.RED + "Your home was unset, so your " + timer.getName() + ChatColor.RED + " timer has been cancelled");
                }
            }
        }

        this.home = home == null ? null : new PersistableLocation(home);
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(@Nullable String announcement) {
        this.announcement = announcement;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    @Override
    public boolean isRaidable() {
        return deathsUntilRaidable <= 0;
    }

    @Override
    public double getDeathsUntilRaidable() {
        return this.getDeathsUntilRaidable(true);
    }

    @Override
    public double getMaximumDeathsUntilRaidable() {
        return members.size() == 1 ? 1.1 : Math.min(Configuration.FACTION_MAXIMUM_DTR, members.size() * 0.9);
    }

    public double getDeathsUntilRaidable(boolean updateLastCheck) {
        if (updateLastCheck) this.updateDeathsUntilRaidable();
        return deathsUntilRaidable;
    }

    public ChatColor getDtrColour() {
        this.updateDeathsUntilRaidable();
        if (deathsUntilRaidable < 0) {
            return ChatColor.RED;
        } else if (deathsUntilRaidable < 1) {
            return ChatColor.YELLOW;
        } else {
            return ChatColor.GREEN;
        }
    }

    @Getter
    private long lastDtrUpdateTimestamp;

    /**
     * Updates the deaths until raidable value depending
     * how much is gained every x seconds as set in configuration.
     */
    private void updateDeathsUntilRaidable() {
        if (this.getRegenStatus() == RegenStatus.REGENERATING) {
            long now = System.currentTimeMillis();
            long millisPassed = now - this.lastDtrUpdateTimestamp;
            long millisBetweenUpdates = Configuration.FACTION_DTR_UPDATE_MILLIS;
            if (millisPassed >= millisBetweenUpdates) {
                long remainder = millisPassed % millisBetweenUpdates;  // the remaining time until the next update
                int multiplier = (int) (((double) millisPassed + remainder) / millisBetweenUpdates);
                this.lastDtrUpdateTimestamp = now - remainder;
                this.setDeathsUntilRaidable(this.deathsUntilRaidable + (multiplier * millisBetweenUpdates));
            }
        }
    }

    @Override
    public double setDeathsUntilRaidable(double deathsUntilRaidable) {
        return this.setDeathsUntilRaidable(deathsUntilRaidable, true);
    }

    private double setDeathsUntilRaidable(double deathsUntilRaidable, boolean limit) {
        deathsUntilRaidable = Math.round(deathsUntilRaidable * 100.0) / 100.0; // remove trailing numbers after decimal
        if (limit) {
            deathsUntilRaidable = Math.min(deathsUntilRaidable, getMaximumDeathsUntilRaidable());
        }

        // the DTR is the same, don't call an event
        if (Math.abs(deathsUntilRaidable - this.deathsUntilRaidable) != 0) {
            FactionDtrChangeEvent event = new FactionDtrChangeEvent(FactionDtrChangeEvent.DtrUpdateCause.REGENERATION, this, this.deathsUntilRaidable, deathsUntilRaidable);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                deathsUntilRaidable = Math.round(event.getNewDtr() * 100.0) / 100.0;
                if (deathsUntilRaidable > 0 && this.deathsUntilRaidable <= 0) {
                    // Inform the server for easier log lookups for 'insiding' etc.
                    Apollo.getInstance().getLogger().info("Faction " + getName() + " is now raidable.");
                }

                this.lastDtrUpdateTimestamp = System.currentTimeMillis();
                return this.deathsUntilRaidable = deathsUntilRaidable;
            }
        }

        return this.deathsUntilRaidable;
    }

    protected long getRegenCooldownTimestamp() {
        return regenCooldownTimestamp;
    }

    @Override
    public long getRemainingRegenerationTime() {
        return regenCooldownTimestamp == 0L ? 0L : regenCooldownTimestamp - System.currentTimeMillis();
    }

    @Override
    public void setRemainingRegenerationTime(long millis) {
        long systemMillis = System.currentTimeMillis();
        this.regenCooldownTimestamp = systemMillis + millis;

        // needs to be multiplied by 2 because as soon as they lose regeneration delay, the timestamp will update
        this.lastDtrUpdateTimestamp = systemMillis + (Configuration.FACTION_DTR_UPDATE_MILLIS * 2);
    }

    @Override
    public RegenStatus getRegenStatus() {
        if (getRemainingRegenerationTime() > 0L) {
            return RegenStatus.PAUSED;
        } else if (getMaximumDeathsUntilRaidable() > this.deathsUntilRaidable) {
            return RegenStatus.REGENERATING;
        } else {
            return RegenStatus.FULL;
        }
    }

    @Override
    public void printDetails(CommandSender sender) {

        Set<String> allyNames = new HashSet<>();
        for (Map.Entry<ObjectId, Relation> entry : relations.entrySet()) {
            Apollo.getInstance().getFactionDao().get(entry.getKey()).filter(faction -> faction instanceof PlayerFaction).ifPresent(faction -> {
                allyNames.add(faction.getDisplayName(sender) + ChatColor.AQUA +
                        '[' + ChatColor.WHITE + "" + (String.valueOf(((PlayerFaction) faction).getOnlineMembers(sender).size()) + ChatColor.GRAY + '/' + ChatColor.WHITE + ((PlayerFaction) faction).getMembers().size() + ChatColor.AQUA + ']'));
            });
        }

        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);

        // Show the banner with the Home location.
        sender.sendMessage(ChatColor.WHITE + " " + getDisplayName(sender) +
                ChatColor.AQUA + " (" + ChatColor.WHITE + getOnlinePlayers(sender).size() + ChatColor.GRAY + '/' + ChatColor.WHITE + this.members.size() + ChatColor.AQUA + ") " +
                ChatColor.GRAY + " | " + ChatColor.AQUA + "Home: " + ChatColor.WHITE + (home == null ? "None" : "(" + home.getLocation().getBlockX() + ChatColor.GRAY + " | " + ChatColor.WHITE + home.getLocation().getBlockZ() + ')'));

        sender.sendMessage();

        // Show announcement if the sender is in this faction.
        if (sender instanceof Player) {
            Apollo.getInstance().getFactionDao().getByPlayer(((Player) sender).getUniqueId()).filter(faction -> faction instanceof PlayerFaction && announcement != null).ifPresent(faction -> sender.sendMessage(ChatColor.AQUA + "Announcement: " + ChatColor.WHITE + announcement));
        }

        if (!allyNames.isEmpty()) sender.sendMessage(ChatColor.AQUA + " Allies: " + ChatColor.WHITE + StringUtils.join(allyNames, ','));

        sender.sendMessage(ChatColor.AQUA + " Online Members: " + this.members.stream().filter(member -> member.toOnlinePlayer() != null).map(member -> {
            User user = Apollo.getInstance().getUserDao().get(member.getUniqueID()).get();
            return ChatColor.GREEN + member.getRole().getAstrix() + member.getName() + ChatColor.AQUA + " [" + ChatColor.WHITE + member.toOnlinePlayer().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.AQUA + ']';
        }).collect(Collectors.joining(", ")));
        sender.sendMessage(ChatColor.AQUA + " Offline Members: " + this.members.stream().filter(member -> member.toOnlinePlayer() == null).map(member -> {
            User user = Apollo.getInstance().getUserDao().get(member.getUniqueID()).get();
            return (user.getDeathban() != null && user.getDeathban().isActive() ? ChatColor.RED : ChatColor.GRAY) + member.getRole().getAstrix() + member.getName() + ChatColor.AQUA + " [" + ChatColor.WHITE + user.getPlayer().getStatistic(Statistic.PLAYER_KILLS) + ChatColor.AQUA + ']';
        }).collect(Collectors.joining(", ")));

        sender.sendMessage(ChatColor.AQUA + " Balance: " + ChatColor.WHITE + Configuration.ECONOMY_SYMBOL + balance);
        sender.sendMessage(ChatColor.AQUA + " Created At: " + ChatColor.WHITE + DateTimeFormats.DAY_MTH_YR_HR_MIN_AMPM.format(creationMillis));
        sender.sendMessage(ChatColor.AQUA + " Deaths Until Raidable: [" +
                getRegenStatus().getSymbol() + getDtrColour() + String.format("%.2f", getDeathsUntilRaidable(false)) +
                ChatColor.GRAY + '/' + ChatColor.AQUA + String.format("%.2f", getMaximumDeathsUntilRaidable()) + ']');

        long dtrRegenRemaining = getRemainingRegenerationTime();
        if (dtrRegenRemaining > 0L) {
            sender.sendMessage(ChatColor.AQUA + " DTR Regeneration Time: " + ChatColor.WHITE + DurationFormatUtils.formatDurationWords(dtrRegenRemaining, true, true));
        }

        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    private static final UUID[] EMPTY_UUID_ARRAY = {};

    /**
     * Sends a message to all online {@link FactionMember}s.
     *
     * @param message the message to send
     */
    public void broadcast(String message) {
        broadcast(message, EMPTY_UUID_ARRAY);
    }

    /**
     * Sends an array of messages to all online {@link FactionMember}s.
     *
     * @param messages the messages to send.
     */
    public void broadcast(String[] messages) {
        broadcast(messages, EMPTY_UUID_ARRAY);
    }

    /**
     * Sends a message to all online {@link FactionMember}s ignoring those selected in the var-args.
     *
     * @param message the message to send.
     * @param ignore  the {@link FactionMember} with {@link UUID}s not to send message to
     */
    public void broadcast(String message, @Nullable UUID... ignore) {
        this.broadcast(new String[]{message}, ignore);
    }

    /**
     * Sends an array of messages to all online {@link FactionMember}s ignoring those selected in the var-args.
     *
     * @param messages the message to send
     * @param ignore   the {@link FactionMember} with {@link UUID}s not to send message to
     */
    public void broadcast(@NonNull String[] messages, @NonNull UUID... ignore) {
        Objects.requireNonNull(messages.length > 0, "Message array cannot be empty");
        Collection<UUID> ignores = ignore.length == 0 ? Collections.emptySet() : Sets.newHashSet(ignore);
        for (Player player : this.getOnlinePlayers()) {
            if (!ignores.contains(player.getUniqueId())) {
                player.sendMessage(messages);
            }
        }
    }
}
