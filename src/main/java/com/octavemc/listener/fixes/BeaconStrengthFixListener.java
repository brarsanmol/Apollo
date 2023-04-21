package com.octavemc.listener.fixes;

import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BeaconStrengthFixListener implements Listener {

    private static final int VANILLA_BEACON_STRENGTH_LIMIT = 2;

    private final Apollo plugin;

    public BeaconStrengthFixListener(Apollo plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPotionEffectAdd(BeaconEffectEvent event) {
        int limit = Configuration.BEACON_STRENGTH_LEVEL_LIMIT;

        if (limit <= 0) {
            event.setCancelled(true);
            return;
        }

        //TODO: Remove this?
        limit--; // we do this because the numbering for potion effects are weird in bukkit
        // amplifier of 0 is a level 1 potion, amplifier of 1 is a level 2, etc..
        // so let's not confuse the config editor.
        if (limit >= BeaconStrengthFixListener.VANILLA_BEACON_STRENGTH_LIMIT) {
            return;
        }

        PotionEffect effect = event.getEffect();
        if (effect.getAmplifier() > limit && effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
            event.getPlayer().addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration(), limit, effect.isAmbient()));
            event.setCancelled(true);
        }
    }
}
