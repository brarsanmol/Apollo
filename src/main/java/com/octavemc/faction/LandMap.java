package com.octavemc.faction;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.claim.Claim;
import com.octavemc.faction.claim.ClaimHandler;
import com.octavemc.faction.type.Faction;
import com.octavemc.faction.type.PlayerFaction;
import com.octavemc.util.BukkitUtils;
import com.octavemc.util.NameUtils;
import com.octavemc.visualise.VisualBlockData;
import com.octavemc.visualise.VisualType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class LandMap {

    private static final int FACTION_MAP_RADIUS_BLOCKS = 32;

    /**
     * Updates the {@link Faction} {@link Claim} map for a {@link Player}.
     *
     * @param player     the {@link Player} to update for
     * @param plugin     the {@link org.bukkit.plugin.java.JavaPlugin} to update for
     * @param visualType the {@link VisualType} to update for
     * @param inform     if the {@link VisualType} should be informed
     * @return true if their are {@link Claim}s to update the map for
     */
    public static boolean updateMap(Player player, Apollo plugin, VisualType visualType, boolean inform) {
        Location location = player.getLocation();
        World world = player.getWorld();

        final Set<Claim> board = new LinkedHashSet<>();
        final boolean subclaimBased;
        switch (visualType) {
            case SUBCLAIM_MAP -> subclaimBased = true;
            case CLAIM_MAP -> subclaimBased = false;
            default -> {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The claim type " + ChatColor.AQUA + visualType.name().toLowerCase() + ChatColor.GRAY + " is not supported.");
                return false;
            }
        }
        
        for (int x = (location.getBlockX() - FACTION_MAP_RADIUS_BLOCKS); x <= (location.getBlockX() + FACTION_MAP_RADIUS_BLOCKS); x++) {
            for (int z = (location.getBlockZ() - FACTION_MAP_RADIUS_BLOCKS); z <= (location.getBlockZ() + FACTION_MAP_RADIUS_BLOCKS); z++) {
                Apollo.getInstance().getFactionDao().getClaimAt(world, x, z).ifPresent(claim -> {
                    if (subclaimBased) board.addAll(claim.getSubclaims());
                    else board.add(claim);
                });
            }
        }

        if (board.isEmpty()) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "No claims to show within a " + ChatColor.AQUA + FACTION_MAP_RADIUS_BLOCKS + ChatColor.GRAY + " block radius.");
            return false;
        }

        for (Claim claim : board) {
            int maxHeight = Math.min(world.getMaxHeight(), ClaimHandler.MAX_CLAIM_HEIGHT);
            Location[] corners = claim.getCornerLocations();
            List<Location> shown = new ArrayList<>(maxHeight * corners.length);
            for (Location corner : corners) {
                for (int y = 0; y < maxHeight; y++) {
                    shown.add(world.getBlockAt(corner.getBlockX(), y, corner.getBlockZ()).getLocation());
                }
            }

            Map<Location, VisualBlockData> dataMap = plugin.getVisualiseHandler().generate(player, shown, visualType, true);
            if (dataMap.isEmpty()) continue;

            if (inform) player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + claim.getFaction().getDisplayName(player) + ChatColor.GRAY + " owns the territory " + ChatColor.AQUA + claim.getName() + ", displayed with " + ChatColor.AQUA + NameUtils.getPrettyName(dataMap.entrySet().iterator().next().getValue().getItemType().name()) + ChatColor.GRAY + '.');
        }

        return true;
    }

    /**
     * Finds the nearest safe {@link Location} from a given position.
     *
     * @param player       the {@link Player} to find for
     * @param origin       the {@link Location} to begin searching at
     * @param searchRadius the radius to search for
     * @return the nearest safe {@link Location} from origin
     */
    public static Location getNearestSafePosition(Player player, Location origin, int searchRadius) {
        PlayerFaction faction = Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).get();

        for (int x = origin.getBlockX() - searchRadius; x < origin.getBlockX() + searchRadius; x++) {
            for (int z = origin.getBlockZ() - searchRadius; z < origin.getBlockZ() + searchRadius; z++) {
                Location atPos = origin.clone().add(x, 0, z);
                Faction factionAtPos = Apollo.getInstance().getFactionDao().getFactionAt(atPos).get();
                if (Objects.equals(factionAtPos, faction) || !(factionAtPos instanceof PlayerFaction)) {
                    return BukkitUtils.getHighestLocation(atPos, atPos);
                }

                Location atNeg = origin.clone().add(x, 0, z);
                Faction factionAtNeg = Apollo.getInstance().getFactionDao().getFactionAt(atNeg).get();
                if (Objects.equals(factionAtNeg, faction) || !(factionAtNeg instanceof PlayerFaction)) {
                    return BukkitUtils.getHighestLocation(atNeg, atNeg);
                }
            }
        }

        return null;
    }
}
