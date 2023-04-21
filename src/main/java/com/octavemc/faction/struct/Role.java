package com.octavemc.faction.struct;

import com.octavemc.faction.type.Faction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Represents the {@link Role} of a {@link org.bukkit.entity.Player} in a {@link Faction}.
 */
@AllArgsConstructor
@Getter
public enum Role {

    LEADER("Leader", "**", 3),
    CAPTAIN("Captain", "*", 2),
    MEMBER("Member", "", 1);

    private final String name;
    private final String astrix;
    private final int power;

    //TODO: Remove this botch
    public static Role getRoleByPower(int power) {
        return Arrays.stream(values()).filter(role -> role.getPower() == power).findFirst().orElse(MEMBER);
    }
}
