package com.octavemc.faction.type;

import com.octavemc.Configuration;
import org.bukkit.command.CommandSender;

/**
 * Represents the {@link WarzoneFaction}.
 */
public class WarzoneFaction extends Faction {

    public WarzoneFaction() {
        super("Warzone");
    }

    @Override
    public String getDisplayName(CommandSender sender) {
        return Configuration.RELATION_COLOUR_WARZONE + getName();
    }
}
