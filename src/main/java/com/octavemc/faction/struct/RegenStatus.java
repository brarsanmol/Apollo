package com.octavemc.faction.struct;

import com.octavemc.faction.type.Faction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

/**
 * Represents the {@link RegenStatus} of a {@link Faction}.
 */
@AllArgsConstructor
@Getter
public enum RegenStatus {

    FULL(ChatColor.GREEN.toString() + '\u25B6'),
    REGENERATING(ChatColor.GOLD.toString() + '\u21ea'),
    PAUSED(ChatColor.RED.toString() + '\u25a0');

    private final String symbol;
}
