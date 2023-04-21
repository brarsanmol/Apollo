package com.octavemc.faction.type;

import com.octavemc.Configuration;
import org.bukkit.command.CommandSender;

/**
 * Represents the {@link WildernessFaction}.
 */
public class WildernessFaction extends Faction {

    public WildernessFaction() {
        super("Wilderness");
    }

    @Override
    public String getDisplayName(CommandSender sender) {
        return Configuration.RELATION_COLOUR_WILDERNESS + getName();
    }
}
