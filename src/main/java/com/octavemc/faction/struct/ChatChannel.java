package com.octavemc.faction.struct;

import com.octavemc.Configuration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Locale;

@AllArgsConstructor
@Getter
public enum ChatChannel {

    FACTION("Faction"), ALLIANCE("Alliance"), PUBLIC("Public");

    private final String name;

    public String getDisplayName() {
        switch (this) {
            case FACTION:
                return Configuration.RELATION_COLOUR_TEAMMATE.toString() + name;
            case ALLIANCE:
                return Configuration.RELATION_COLOUR_ALLY.toString() + name;
            case PUBLIC:
            default:
                return Configuration.RELATION_COLOUR_ENEMY.toString() + name;
        }
    }

    /**
     * Parse an {@link ChatChannel} from a String.
     *
     * @param id  the id to search
     * @param def the default {@link ChatChannel} if null
     * @return the {@link ChatChannel} by name
     */
    public static ChatChannel parse(String id, ChatChannel def) {
        id = id.toLowerCase(Locale.ENGLISH);
        switch (id) {
            case "f":
            case "faction":
            case "fc":
            case "fac":
            case "fact":
                return ChatChannel.FACTION;
            case "a":
            case "alliance":
            case "ally":
            case "ac":
                return ChatChannel.ALLIANCE;
            case "p":
            case "pc":
            case "g":
            case "gc":
            case "global":
            case "pub":
            case "publi":
            case "public":
                return ChatChannel.PUBLIC;
            default:
                return def == null ? null : def.getRotation();
        }
    }

    /**
     * Gets the next {@link ChatChannel} from the current.
     *
     * @return the next rotation value
     */
    public ChatChannel getRotation() {
        switch (this) {
            case FACTION:
                return PUBLIC;
            case PUBLIC:
                return Configuration.FACTION_MAXIMUM_ALLIES > 0 ? ALLIANCE : FACTION;
            case ALLIANCE:
                return FACTION;
            default:
                return PUBLIC;
        }
    }

    public String getRawFormat(Player player) {
        switch (this) {
            case FACTION:
            case ALLIANCE:
                return Configuration.RELATION_COLOUR_TEAMMATE + "(" + getDisplayName() + Configuration.RELATION_COLOUR_TEAMMATE + ") " + player.getName() + ChatColor.GRAY + ": " + ChatColor.YELLOW + "%2$s";
            default:
                throw new IllegalArgumentException("Cannot get the raw format for public chat channel");
        }
    }
}
