package com.octavemc.pvpclass.type;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.pvpclass.PvpClass;
import com.octavemc.util.ParticleEffect;
import net.minecraft.server.v1_8_R3.EnumParticle;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

public class RogueClass extends PvpClass implements Listener {

    private final Apollo plugin;

    public RogueClass(Apollo plugin) {
        super("Rogue", TimeUnit.SECONDS.toMillis(15L));

        this.plugin = plugin;
        this.passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player && event.getDamager() instanceof Player attacker) {
            if (plugin.getPvpClassManager().getEquippedClass(attacker) == this) {
                ItemStack stack = attacker.getItemInHand();
                if (stack != null && stack.getType() == Material.GOLD_SWORD && stack.getEnchantments().isEmpty()) {
                    ChatColor relationColourEnemy = Configuration.RELATION_COLOUR_ENEMY;

                    player.sendMessage(relationColourEnemy + attacker.getName() + ChatColor.YELLOW + " has backstabbed you.");
                    player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);

                    attacker.sendMessage(ChatColor.YELLOW + "You have backstabbed " + relationColourEnemy + player.getName() + ChatColor.YELLOW + '.');
                    attacker.setItemInHand(new ItemStack(Material.AIR, 1));
                    attacker.playSound(player.getLocation(), Sound.ITEM_BREAK, 1.0F, 1.0F);

                    //TODO: Remove mark for special arrows on archer...
                    if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                        event.setDamage(10.0F);
                    }
                    event.setDamage(6.0F);
                }
            }
        }

        if (event.getDamager() instanceof Snowball snowball && snowball.getShooter() instanceof Player attacker && Apollo.getInstance().getPvpClassManager().getEquippedClass(attacker) == this) {
            attacker.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY +"You have been teleported to " + ChatColor.AQUA + event.getEntity().getName() + ChatColor.GRAY + '.');
            attacker.teleport(event.getEntity().getLocation().add(0, 1, 0));
            attacker.setVelocity(snowball.getLocation().toVector().subtract(event.getEntity().getLocation().toVector()).multiply(0.37));
            ParticleEffect.sphere(EnumParticle.SNOWBALL, null, event.getEntity().getLocation(), 4);
        }
    }

    @Override
    public boolean isApplicableFor(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        ItemStack helmet = playerInventory.getHelmet();
        if (helmet == null || helmet.getType() != Material.CHAINMAIL_HELMET) {
            return false;
        }

        ItemStack chestplate = playerInventory.getChestplate();
        if (chestplate == null || chestplate.getType() != Material.CHAINMAIL_CHESTPLATE) {
            return false;
        }

        ItemStack leggings = playerInventory.getLeggings();
        if (leggings == null || leggings.getType() != Material.CHAINMAIL_LEGGINGS) {
            return false;
        }

        ItemStack boots = playerInventory.getBoots();
        return !(boots == null || boots.getType() != Material.CHAINMAIL_BOOTS);
    }
}
