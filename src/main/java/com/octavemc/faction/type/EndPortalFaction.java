package com.octavemc.faction.type;

import com.octavemc.Configuration;
import com.octavemc.faction.claim.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents the {@link EndPortalFaction}.
 */
public class EndPortalFaction extends ClaimableFaction {

    public EndPortalFaction() {
        super("End Portal");

        World overworld = Bukkit.getWorld("world");
        int maxHeight = overworld.getMaxHeight();
        int min = Configuration.END_PORTAL_CENTER - Configuration.END_PORTAL_RADIUS;
        int max = Configuration.END_PORTAL_CENTER + Configuration.END_PORTAL_RADIUS;

        // North East (++)
        addClaim(new Claim(this, new Location(overworld, min, 0, min), new Location(overworld, max, maxHeight, max)), null);

        // South West (--)
        addClaim(new Claim(this, new Location(overworld, -max, maxHeight, -max), new Location(overworld, -min, 0, -min)), null);

        // North West (-+)
        addClaim(new Claim(this, new Location(overworld, -max, 0, min), new Location(overworld, -min, maxHeight, max)), null);

        // South East (+-)
        addClaim(new Claim(this, new Location(overworld, min, 0, -max), new Location(overworld, max, maxHeight, -min)), null);

        this.safezone = true;
    }

    @Override
    public boolean isDeathban() {
        return false;
    }
}
