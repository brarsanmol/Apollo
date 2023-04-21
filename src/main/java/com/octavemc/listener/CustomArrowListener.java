package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.pvpclass.archer.ArcherClass;
import com.octavemc.pvpclass.archer.CustomArrows;
import com.octavemc.util.NameUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.metadata.FixedMetadataValue;

public class CustomArrowListener implements Listener {

    public CustomArrowListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
        for (CustomArrows type : CustomArrows.values()) {
            Apollo.getInstance().getServer().addRecipe(new ShapedRecipe(type.getStack())
                    .shape(" A ", "APA", " A ")
                    .setIngredient('A', Material.ARROW)
                    .setIngredient('P', Material.getMaterial(373), type.getPotion()));
        }
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityShootBowEvent(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player
                && Apollo.getInstance().getPvpClassManager().getEquippedClass(player) instanceof ArcherClass) {
            ItemStack stack = player.getInventory().getItem(player.getInventory().first(Material.ARROW));
            if (stack.hasItemMeta()) {
                for (CustomArrows arrow : CustomArrows.values()) {
                    if (event.getBow().containsEnchantment(Enchantment.ARROW_INFINITE)) stack.setAmount(stack.getAmount() - 1);
                    if (NameUtils.getPrettyName(arrow.name()).equalsIgnoreCase(ChatColor.stripColor(stack.getItemMeta().getDisplayName()))) {
                        event.getProjectile().setMetadata("arrow", new FixedMetadataValue(Apollo.getInstance(), arrow.ordinal()));
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileHitEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow arrow
                && arrow.getShooter() instanceof Player damager
                && event.getEntity() instanceof Player player
                && arrow.hasMetadata("arrow")) {
            CustomArrows type = CustomArrows.values()[arrow.getMetadata("arrow").get(0).asInt()];
            player.addPotionEffect(type.getEffect());
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A archer has marked you with a " + ChatColor.AQUA + NameUtils.getPrettyName(type.name()) + ChatColor.GRAY + '.');
            damager.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have marked " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + "with a " + ChatColor.AQUA + NameUtils.getPrettyName(type.name()) + ChatColor.GRAY + '.');
        }
    }
}
