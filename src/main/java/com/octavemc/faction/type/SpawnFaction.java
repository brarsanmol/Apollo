package com.octavemc.faction.type;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.claim.Claim;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents the {@link SpawnFaction}.
 */
public class SpawnFaction extends ClaimableFaction {

    public SpawnFaction() {
        super("Spawn");

        this.safezone = true;
        for (World world : Apollo.getInstance().getServer().getWorlds()) {
            int radius = Configuration.SPAWN_RADIUS.get(world.getEnvironment());
            if (radius > 0) addClaim(new Claim(this, new Location(world, radius, 0, radius), new Location(world, -radius, world.getMaxHeight(), -radius)), null);
        }
    }

    @Override
    public boolean isDeathban() {
        return false;
    }
}
