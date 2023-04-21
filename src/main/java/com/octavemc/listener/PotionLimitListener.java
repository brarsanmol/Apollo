package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.Arrays;

/**
 * Listener that prevents the brewing of illegal {@link org.bukkit.potion.PotionEffectType}s.
 */
public class PotionLimitListener implements Listener {

    private static final int EMPTY_BREW_TIME = 400;

    public PotionLimitListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBrew(BrewEvent event) {
        // TODO: BROKEN: event.getContents().getContents() unknown results
        if (!testValidity(event.getContents().getContents())) {
            event.setCancelled(true);
            event.getContents().getHolder().setBrewingTime(EMPTY_BREW_TIME);
        }
    }

    private boolean testValidity(ItemStack[] contents) {
        return Arrays.stream(contents)
                .filter(stack -> stack != null
                        && stack.getType() == Material.POTION
                        && stack.getDurability() != 0)
                .map(Potion::fromItemStack)
                .filter(potion ->potion.getType() != null
                        || !(potion.getType() == PotionType.POISON
                        && !potion.hasExtendedDuration() && potion.getLevel() == 1)
                        || potion.getLevel() > Configuration.getPotionLimit(potion.getType()))
                .count() == 0;
    }
}
