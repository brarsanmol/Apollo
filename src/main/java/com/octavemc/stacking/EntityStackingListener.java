package com.octavemc.stacking;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.util.NameUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityStackingListener implements Listener {

    public EntityStackingListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (Configuration.STACKABLE_ENTITIES.contains(event.getEntity().getType()))  {
            LivingEntity entity = event.getEntity();
            int count = entity.getCustomName() == null ? 1 : Integer.parseInt(ChatColor.stripColor(entity.getCustomName().substring(entity.getCustomName().lastIndexOf('(') + 1, entity.getCustomName().lastIndexOf('x'))));
            if (count > 1) {
                LivingEntity clone = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());
                clone.setCustomName(ChatColor.AQUA + NameUtils.getPrettyName(entity.getType().name()) + ChatColor.GRAY + " (" + ChatColor.AQUA + (count - 1) + 'x' + ChatColor.GRAY + ')');
                clone.setCustomNameVisible(true);
            }
        }
    }

}
