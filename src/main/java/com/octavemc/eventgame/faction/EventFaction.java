package com.octavemc.eventgame.faction;

import com.octavemc.eventgame.CaptureZone;
import com.octavemc.eventgame.EventType;
import com.octavemc.faction.claim.Claim;
import com.octavemc.faction.claim.ClaimHandler;
import com.octavemc.faction.type.ClaimableFaction;
import com.octavemc.faction.type.Faction;
import com.octavemc.util.cuboid.Cuboid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.List;

@NoArgsConstructor
public abstract class EventFaction extends ClaimableFaction {

    @Getter @Setter
    private String crate;

    public EventFaction(String name, String crate) {
        super(name);
        setDeathban(true); // make cappable factions death-ban between reloads.
        this.crate = crate;
    }

    @Override
    public String getDisplayName(Faction faction) {
        return ChatColor.AQUA + getName() + ' ' + ChatColor.WHITE + getEventType().getDisplayName();
    }

    @Override
    public String getDisplayName(CommandSender sender) {
        return ChatColor.AQUA + getName();
    }

    /**
     * Sets the {@link Cuboid} area of this {@link KothFaction}.
     *
     * @param cuboid the {@link Cuboid} to set
     * @param sender the {@link CommandSender} setting the claim
     */
    public void setClaim(Cuboid cuboid, CommandSender sender) {
        removeClaims(getClaims(), sender);

        // Now add the new claim.
        Location min = cuboid.getMinimumPoint();
        min.setY(ClaimHandler.MIN_CLAIM_HEIGHT);

        Location max = cuboid.getMaximumPoint();
        max.setY(ClaimHandler.MAX_CLAIM_HEIGHT);

        addClaim(new Claim(this, min, max), sender);
    }

    /**
     * Gets the {@link EventType} of this {@link CapturableFaction}.
     *
     * @return the {@link EventType}
     */
    public abstract EventType getEventType();

    /**
     * Gets the {@link CaptureZone}s for this {@link CapturableFaction}.
     *
     * @return list of {@link CaptureZone}s
     */
    public abstract List<CaptureZone> getCaptureZones();
}
