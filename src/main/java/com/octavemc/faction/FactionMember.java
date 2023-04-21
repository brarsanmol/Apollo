package com.octavemc.faction;

import com.octavemc.faction.struct.ChatChannel;
import com.octavemc.faction.struct.Role;
import com.octavemc.faction.type.Faction;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Stores data about members in a {@link Faction}.
 */
@NoArgsConstructor
@AllArgsConstructor
@Embedded
@Data
public class FactionMember {

    @Property
    private UUID uniqueID;
    @Property
    private ChatChannel chatChannel;
    @Property
    private Role role;

    /**
     * Gets the name of this {@link FactionMember}.
     *
     * @return the name of this {@link FactionMember}
     */
    public String getName() {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uniqueID);
        return player.hasPlayedBefore() || player.isOnline() ? player.getName() : null;
    }

    /**
     * Converts this {@link Player} to a {@link Player}.
     *
     * @return the {@link Player} or null if not found
     */
    public Player toOnlinePlayer() {
        return Bukkit.getPlayer(uniqueID);
    }
}