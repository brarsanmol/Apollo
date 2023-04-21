package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.entity.Squid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Listener that limits the amount of entities in one chunk.
 */
public class EntityLimitListener implements Listener {

    //TODO: Move this to Configuration.java?
    private static final int MAX_CHUNK_GENERATED_ENTITIES = 25;
    private static final int MAX_NATURAL_CHUNK_ENTITIES = 25;

    public EntityLimitListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (Configuration.HANDLE_ENTITY_LIMITING) {
            if (event.getEntity() instanceof Squid) {
                event.setCancelled(true);
                return;
            }
            if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) { // allow slimes to always split
                switch (event.getSpawnReason()) {
                    case NATURAL:
                        if (event.getLocation().getChunk().getEntities().length > MAX_NATURAL_CHUNK_ENTITIES) {
                            event.setCancelled(true);
                        }
                        break;
                    case CHUNK_GEN:
                        if (event.getLocation().getChunk().getEntities().length > MAX_CHUNK_GENERATED_ENTITIES) {
                            event.setCancelled(true);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
