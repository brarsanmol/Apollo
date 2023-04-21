package com.octavemc.eventgame.faction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.octavemc.eventgame.CaptureZone;
import com.octavemc.eventgame.EventType;
import com.octavemc.faction.type.ClaimableFaction;
import com.octavemc.util.BukkitUtils;
import dev.morphia.annotations.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Represents a 'Conquest' faction.
 */
@NoArgsConstructor
public class ConquestFaction extends CapturableFaction {

    @AllArgsConstructor
    @Getter
    public enum ConquestZone {

        RED("Red", ChatColor.RED),
        BLUE("Blue", ChatColor.AQUA),
        YELLOW("Yellow", ChatColor.YELLOW),
        GREEN("Green", ChatColor.GREEN);

        private final String name;
        private final ChatColor color;

        public String getDisplayName() {
            return color.toString() + name;
        }

        private static final Map<String, ConquestZone> BY_NAME;

        static {
            ImmutableMap.Builder<String, ConquestZone> builder = ImmutableMap.builder();
            for (ConquestZone zone : values()) {
                builder.put(zone.name().toUpperCase(), zone);
            }

            BY_NAME = builder.build();
        }

        public static ConquestZone getByName(String name) {
            return BY_NAME.get(name.toUpperCase());
        }

        public static Collection<String> getNames() {
            return new ArrayList<>(BY_NAME.keySet());
        }
    }

    @Property
    private final Map<ConquestZone, CaptureZone> captureZones = new EnumMap<>(ConquestZone.class);

    public ConquestFaction(String name, String crate) {
        super(name, crate);
    }

    @Override
    public EventType getEventType() {
        return EventType.CONQUEST;
    }

    @Override
    public void printDetails(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(getDisplayName(sender));

        claims.forEach(claim ->
                sender.sendMessage(ChatColor.AQUA + "Location: " + ChatColor.WHITE +
                        '(' + ClaimableFaction.ENVIRONMENT_MAPPINGS.get(claim.getCenter().getWorld().getEnvironment()) + ", " + claim.getCenter().getBlockX() + " | " + claim.getCenter().getBlockZ() + ')')
        );

        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    public void setZone(ConquestZone conquestZone, CaptureZone captureZone) {
        switch (conquestZone) {
            case RED:
                captureZones.put(ConquestZone.RED, captureZone);
                break;
            case BLUE:
                captureZones.put(ConquestZone.BLUE, captureZone);
                break;
            case GREEN:
                captureZones.put(ConquestZone.GREEN, captureZone);
                break;
            case YELLOW:
                captureZones.put(ConquestZone.YELLOW, captureZone);
                break;
            default:
                throw new AssertionError("Unsupported operation");
        }
    }

    public CaptureZone getRed() {
        return captureZones.get(ConquestZone.RED);
    }

    public CaptureZone getGreen() {
        return captureZones.get(ConquestZone.GREEN);
    }

    public CaptureZone getBlue() {
        return captureZones.get(ConquestZone.BLUE);
    }

    public CaptureZone getYellow() {
        return captureZones.get(ConquestZone.YELLOW);
    }

    public Collection<ConquestZone> getConquestZones() {
        return ImmutableSet.copyOf(captureZones.keySet());
    }

    @Override
    public List<CaptureZone> getCaptureZones() {
        return ImmutableList.copyOf(captureZones.values());
    }
}
