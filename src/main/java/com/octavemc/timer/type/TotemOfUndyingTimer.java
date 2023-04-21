package com.octavemc.timer.type;

import com.octavemc.Apollo;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.util.ItemBuilder;
import com.octavemc.util.ParticleEffect;
import com.octavemc.util.Skulls;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

public class TotemOfUndyingTimer extends PlayerTimer implements Listener {

    public static final ItemStack TOTEM_OF_UNDYING_ITEMSTACK = new ItemBuilder(Skulls.getCustomSkull("http://textures.minecraft.net/texture/7dba10a0726b8b6ffaaec268adc01500dcdc393ad039bed6a43adbc43f712cf4"))
            .displayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Totem of Undying")
            .lore(
                    ChatColor.AQUA + "Right Click " + ChatColor.WHITE + "This To Gain Powerful Buffs!",
                    ChatColor.AQUA + "Keep This In Your Inventory " + ChatColor.WHITE + "To Save Yourself From Dying!"
            )
            .build();

    public TotemOfUndyingTimer() {
        super("Totem Of Undying", TimeUnit.MINUTES.toMillis(30L), false);
        Apollo.getInstance().getServer().addRecipe(new ShapedRecipe(TOTEM_OF_UNDYING_ITEMSTACK)
                .shape(" G ", "GSG", " G ")
                .setIngredient('G', Material.GOLD_BLOCK)
                .setIngredient('S', Material.SKULL_ITEM, 3));
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player
                && player.getInventory().contains(TOTEM_OF_UNDYING_ITEMSTACK)
                && player.getHealth() - event.getFinalDamage() <= 0) {
            if (getRemaining(player) == 0L) {
                event.setCancelled(true);
                player.getInventory().getItem(player.getInventory().first(TOTEM_OF_UNDYING_ITEMSTACK)).setAmount(player.getInventory().getItem(player.getInventory().first(TOTEM_OF_UNDYING_ITEMSTACK)).getAmount() - 1);
                player.setHealth(2);
                ParticleEffect.sphere(EnumParticle.FIREWORKS_SPARK, null, player.getLocation(), 5);
                player.getWorld().playSound(player.getLocation(), Sound.FIREWORK_BLAST, 1F, 0.5F);
                player.getNearbyEntities(5, 5, 5).forEach(entity -> entity.setVelocity(player.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(-2).setY(1)));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 10 * 20, 2), true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10 * 20, 2), true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 2), true);
                setCooldown(player, player.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction().name().contains("RIGHT")
                && event.getPlayer().getItemInHand().isSimilar(TOTEM_OF_UNDYING_ITEMSTACK)
                && Apollo.getInstance().getFactionDao().getFactionAt(event.getPlayer().getLocation()).get().isDeathban()) {
            if (getRemaining(event.getPlayer()) == 0L) {
                event.getPlayer().getInventory().getItemInHand().setAmount(event.getPlayer().getInventory().getItemInHand().getAmount() - 1);
                ParticleEffect.sphere(EnumParticle.FIREWORKS_SPARK, null, event.getPlayer().getLocation(), 5);
                event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.FIREWORK_BLAST, 1F, 0.5F);
                event.getPlayer().getNearbyEntities(5, 5, 5).forEach(entity -> entity.setVelocity(event.getPlayer().getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(-2).setY(1)));
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 10 * 20, 4), true);
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 20, 2), true);
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 3), true);
                event.getPlayer().getInventory().remove(TOTEM_OF_UNDYING_ITEMSTACK);
                event.setCancelled(true);
                setCooldown(event.getPlayer(), event.getPlayer().getUniqueId());
            }
        }
    }

}
