package com.octavemc.deathban;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.util.PersistableLocation;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DeathbanManager {

    private static final long MAX_DEATHBAN_TIME = TimeUnit.HOURS.toMillis(8);
    private static final int MAX_DEATHBAN_MULTIPLIER = 300;

    public double getDeathBanMultiplier(Player player) {
        for (int i = 5; i < MAX_DEATHBAN_MULTIPLIER; i++)
            if (player.hasPermission("apollo.deathban.multiplier." + i)) return ((double) i) / 100.0;
        return 1.0D;
    }

    public Deathban applyDeathBan(Player player, String reason) {
        var factionAt = Apollo.getInstance().getFactionDao().getFactionAt(player.getLocation()).get();

        long duration;
        if (Apollo.getInstance().getEotwHandler().isEndOfTheWorld()) {
            duration = MAX_DEATHBAN_TIME;
        } else {
            duration = TimeUnit.MINUTES.toMillis(Configuration.DEATHBAN_BASE_DURATION_MINUTES);
            if (!factionAt.isDeathban()) {
                duration /= 2L; // non-deathban factions should be 50% quicker
            }

            duration *= getDeathBanMultiplier(player);
            duration *= factionAt.getDeathbanMultiplier();
        }

        return applyDeathBan(player.getUniqueId(), new Deathban(reason, Math.min(MAX_DEATHBAN_TIME, duration),
                new PersistableLocation(player.getLocation()), Apollo.getInstance().getEotwHandler().isEndOfTheWorld()));
    }

    public Deathban applyDeathBan(UUID uuid, Deathban deathban) {
        Apollo.getInstance().getUserDao().get(uuid).get().setDeathban(deathban);
        return deathban;
    }
}
