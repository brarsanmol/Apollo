package com.octavemc.faction.type;

import com.octavemc.Configuration;
import com.octavemc.faction.claim.Claim;
import com.octavemc.util.BukkitUtils;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

/**
 * Represents the {@link RoadFaction}.
 * <p>
 * TODO: Needs cleanup
 */
@NoArgsConstructor
public abstract class RoadFaction extends ClaimableFaction {

    // The difference the roads will end from the border.
    public static final int ROAD_EDGE_DIFF = 1000;

    // Represents how wide the roads are.
    public static final int ROAD_WIDTH_LEFT = 24;
    public static final int ROAD_WIDTH_RIGHT = 24;

    // The minimum and maximum heights for roads.
    public static final int ROAD_MIN_HEIGHT = 0; //50 'this allowed people to claim below the roads, temp disabled;
    public static final int ROAD_MAX_HEIGHT = 256; //80 'this allowed people to claim above the roads, temp disabled;

    public RoadFaction(String name) {
        super(name);
    }

    public static class NorthRoadFaction extends RoadFaction {

        public NorthRoadFaction() {
            super("North Road");
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() != World.Environment.THE_END) {
                    int roadLength = Configuration.ROAD_LENGTHS.get(world.getEnvironment());
                    int offset = Configuration.SPAWN_RADIUS.get(world.getEnvironment()) + 1;
                    addClaim(new Claim(this,
                            new Location(world, -ROAD_WIDTH_LEFT, ROAD_MIN_HEIGHT, -offset),
                            new Location(world, ROAD_WIDTH_RIGHT, ROAD_MAX_HEIGHT, -roadLength + ROAD_EDGE_DIFF)), null);
                }
            }
        }
    }

    public static class EastRoadFaction extends RoadFaction {

        public EastRoadFaction() {
            super("East Road");
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() != World.Environment.THE_END) {
                    int roadLength = Configuration.ROAD_LENGTHS.get(world.getEnvironment());
                    int offset = Configuration.SPAWN_RADIUS.get(world.getEnvironment()) + 1;
                    addClaim(new Claim(this,
                            new Location(world, offset, ROAD_MIN_HEIGHT, -ROAD_WIDTH_LEFT),
                            new Location(world, roadLength - ROAD_EDGE_DIFF, ROAD_MAX_HEIGHT, ROAD_WIDTH_RIGHT)), null);
                }
            }
        }
    }

    public static class SouthRoadFaction extends RoadFaction {

        public SouthRoadFaction() {
            super("South Road");
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() != World.Environment.THE_END) {
                    int roadLength = Configuration.ROAD_LENGTHS.get(world.getEnvironment());
                    int offset = Configuration.SPAWN_RADIUS.get(world.getEnvironment()) + 1;
                    addClaim(new Claim(this,
                            new Location(world, ROAD_WIDTH_LEFT, ROAD_MIN_HEIGHT, offset),
                            new Location(world, -ROAD_WIDTH_RIGHT, ROAD_MAX_HEIGHT, roadLength - ROAD_EDGE_DIFF)), null);
                }
            }
        }

    }

    public static class WestRoadFaction extends RoadFaction {

        public WestRoadFaction() {
            super("West Road");
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment() != World.Environment.THE_END) {
                    int roadLength = Configuration.ROAD_LENGTHS.get(world.getEnvironment());
                    int offset = Configuration.SPAWN_RADIUS.get(world.getEnvironment()) + 1;
                    addClaim(new Claim(this,
                            new Location(world, -offset, ROAD_MIN_HEIGHT, ROAD_WIDTH_LEFT),
                            new Location(world, -roadLength + ROAD_EDGE_DIFF, ROAD_MAX_HEIGHT, -ROAD_WIDTH_RIGHT)), null);
                }
            }
        }
    }

    @Override
    public String getDisplayName(CommandSender sender) {
        return Configuration.RELATION_COLOUR_ROAD + getName();
    }

    @Override
    public void printDetails(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(getDisplayName(sender));
        sender.sendMessage(ChatColor.AQUA + "Location: " + ChatColor.WHITE + "None");
        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }
}
