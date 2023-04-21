package com.octavemc.faction.struct;

import com.octavemc.Configuration;
import com.octavemc.faction.type.Faction;
import com.octavemc.util.BukkitUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

/**
 * Represents a relation between {@link Faction}s and {@link org.bukkit.entity.Player}s.
 */
@AllArgsConstructor
@Getter
public enum Relation {

    MEMBER(3), ALLY(2), ENEMY(1);

    private final int value;

    public boolean isAtLeast(Relation relation) {
        return this.value >= relation.value;
    }

    public boolean isAtMost(Relation relation) {
        return this.value <= relation.value;
    }

    public boolean isMember() {
        return this == MEMBER;
    }

    public boolean isAlly() {
        return this == ALLY;
    }

    public boolean isEnemy() {
        return this == ENEMY;
    }

    public String getDisplayName() {
        switch (this) {
            case ALLY:
                return toChatColour() + "alliance";
            default:
                return toChatColour() + name().toLowerCase();
        }
    }

    public ChatColor toChatColour() {
        switch (this) {
            case MEMBER:
                return Configuration.RELATION_COLOUR_TEAMMATE;
            case ALLY:
                return Configuration.RELATION_COLOUR_ALLY;
            case ENEMY:
            default:
                return Configuration.RELATION_COLOUR_ENEMY;
        }
    }

    public DyeColor toDyeColour() {
        return BukkitUtils.toDyeColor(toChatColour());
    }
}
