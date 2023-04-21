package com.octavemc.stacking;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.util.NameUtils;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityStackingTask extends BukkitRunnable {

    public EntityStackingTask() {
        this.runTaskTimerAsynchronously(Apollo.getInstance(), 20L, 10 * 20L);
    }

    @Override
    public void run() {
        for (World world : Apollo.getInstance().getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity) || !entity.isValid()) continue;
                if (!Configuration.STACKABLE_ENTITIES.contains(entity.getType())) continue;
                int count = entity.getCustomName() == null ? 1 : Integer.parseInt(ChatColor.stripColor(entity.getCustomName().substring(entity.getCustomName().lastIndexOf('(') + 1, entity.getCustomName().lastIndexOf('x'))));
                for (Entity nearby : entity.getNearbyEntities(Configuration.STACKING_RADIUS, Configuration.STACKING_RADIUS, Configuration.STACKING_RADIUS)) {
                    if (!(nearby instanceof LivingEntity) || !nearby.isValid()) continue;
                    if (!Configuration.STACKABLE_ENTITIES.contains(nearby.getType())) continue;
                    if (entity.getType() == nearby.getType()) {
                        count += nearby.getCustomName() == null ? 1 : Integer.parseInt(ChatColor.stripColor(nearby.getCustomName().substring(nearby.getCustomName().lastIndexOf('(') + 1, nearby.getCustomName().lastIndexOf('x'))));
                        nearby.remove();
                    }
                }
                entity.setCustomName(ChatColor.AQUA + NameUtils.getPrettyName(entity.getType().name()) + ChatColor.GRAY + " (" + ChatColor.AQUA + count + 'x' + ChatColor.GRAY + ")");
                if (!entity.isCustomNameVisible()) entity.setCustomNameVisible(true);
            }
        }
    }
}
