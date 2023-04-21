package com.octavemc.pvpclass.archer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.pvpclass.PvpClass;
import com.octavemc.pvpclass.event.PvpClassUnequipEvent;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Represents a {@link PvpClass} used to buff Archer game-play such as Bows.
 */
public class ArcherClass extends PvpClass implements Listener {

    private static final PotionEffect ARCHER_CRITICAL_EFFECT = new PotionEffect(PotionEffectType.WITHER, 60, 0);

    private static final int MARK_TIMEOUT_SECONDS = 15;
    private static final int MARK_EXECUTION_LEVEL = 3;
    private static final double MARK_EXECUTION_DAMAGE_BONUS = 2.0;
    private static final float MINIMUM_FORCE = 0.5F;

    private static final PotionEffect ARCHER_SPEED_EFFECT = new PotionEffect(PotionEffectType.SPEED, 160, 3);
    private static final long ARCHER_SPEED_COOLDOWN_DELAY = TimeUnit.MINUTES.toMillis(1L);
    private final TObjectLongMap<UUID> archerSpeedCooldowns = new TObjectLongHashMap<>();

    private final Table<UUID, UUID, ArcherMark> marks = HashBasedTable.create(); // Key of the Players UUID who applied the mark, value as the Mark applied.
    private final Apollo plugin;

    public ArcherClass(Apollo plugin) {
        super("Archer", TimeUnit.SECONDS.toMillis(10L));
        this.plugin = plugin;

        this.passiveEffects.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0));
        this.passiveEffects.add(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2));
    }

    public Map<UUID, ArcherMark> getSentMarks(Player player) {
        synchronized (marks) {
            return marks.column(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerClassUnequip(PvpClassUnequipEvent event) {
        getSentMarks(event.getPlayer()).clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        getSentMarks(event.getEntity()).clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        getSentMarks(event.getPlayer()).clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        getSentMarks(event.getPlayer()).clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void setArrowForce(EntityShootBowEvent event) {
        // Do nothing if the entity being shot was not an arrow.
        if (!(event.getProjectile() instanceof Arrow || event.getEntity() instanceof Player)
                && Apollo.getInstance().getPvpClassManager().getEquippedClass((Player) event.getEntity()) != this) return;
        event.getProjectile().setMetadata("force", new FixedMetadataValue(Apollo.getInstance(), event.getForce()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (entity == damager || !(entity instanceof Player) || !(damager instanceof Arrow)) {
            return;
        }
        Arrow arrow = (Arrow) damager;
        float force = arrow.getMetadata("force").get(0).asFloat();
        if (force == -1.0) {
            return;
        }

        ProjectileSource source = arrow.getShooter();
        if (!(source instanceof Player)) {
            return;
        }
        Player shooter = (Player) source;
        if (!plugin.getPvpClassManager().hasClassEquipped(shooter, this)) {
            return;
        }
        if (force <= MINIMUM_FORCE) {
            shooter.sendMessage(ChatColor.RED + "Mark not applied as arrow was shot with less than " + MINIMUM_FORCE + "% force.");
            return;
        }

        if (arrow.hasMetadata("arrow")) return;

        Player attacked = (Player) entity;
        UUID attackedUUID = attacked.getUniqueId();

        // Get the sent Mark, or create one.
        Map<UUID, ArcherMark> givenMarks = getSentMarks(shooter);
        ArcherMark archerMark = givenMarks.get(attackedUUID);
        if (archerMark != null) {
            archerMark.decrementTask.cancel();
        } else {
            givenMarks.put(attackedUUID, archerMark = new ArcherMark());
        }

        ChatColor enemyColour = Configuration.RELATION_COLOUR_ENEMY;
        int newLevel = archerMark.incrementMark();
        if (newLevel >= MARK_EXECUTION_LEVEL) {
            event.setDamage(event.getDamage(EntityDamageEvent.DamageModifier.BASE) + MARK_EXECUTION_DAMAGE_BONUS);
            attacked.addPotionEffect(ARCHER_CRITICAL_EFFECT);

            getSentMarks(shooter).clear();
            archerMark.reset();

            // Fake the effects.
            World world = attacked.getWorld();
            Location location = attacked.getLocation();
            world.playEffect(location, Effect.EXPLOSION_HUGE, 4);
            world.playSound(location, Sound.EXPLODE, 1.0F, 1.0F);

            attacked.sendMessage(ChatColor.GOLD + "Wipeout! " + enemyColour + shooter.getName() + ChatColor.GOLD + " hit you with a level " +
                    ChatColor.WHITE + ChatColor.GOLD + newLevel + " mark.");
            shooter.sendMessage(ChatColor.YELLOW + "Wipeout! Hit " + enemyColour + attacked.getName() + ChatColor.YELLOW + " with a level " +
                    ChatColor.WHITE + newLevel + ChatColor.YELLOW + " mark.");
        } else {
            event.setDamage(event.getDamage(EntityDamageEvent.DamageModifier.BASE) + 3.0);
            shooter.sendMessage(ChatColor.YELLOW + "Now have a level " + ChatColor.WHITE + newLevel + ChatColor.YELLOW + " mark on " +
                    enemyColour + attacked.getName() + ChatColor.YELLOW + '.');

            final ArcherMark finalMark = archerMark;
            long ticks = MARK_TIMEOUT_SECONDS * 20L;
            archerMark.decrementTask = new BukkitRunnable() {
                @Override
                public void run() {
                    int newLevel = finalMark.decrementMark();
                    if (newLevel == 0) {
                        attacked.sendMessage(enemyColour + shooter.getName() + ChatColor.YELLOW + "'s mark on you has expired.");
                        shooter.sendMessage(ChatColor.GOLD + "No longer have a mark on " + enemyColour + attacked.getName() + ChatColor.GOLD + '.');
                        getSentMarks(shooter).remove(attacked.getUniqueId());
                        cancel();
                    } else {
                        attacked.sendMessage(enemyColour + shooter.getName() + ChatColor.GOLD + "'s mark on you has expired to level " +
                                ChatColor.WHITE + ChatColor.GOLD + newLevel + '.');
                        shooter.sendMessage(ChatColor.YELLOW + "Mark level on " + enemyColour + attacked.getName() + ChatColor.YELLOW + " is now " +
                                ChatColor.WHITE + ChatColor.YELLOW + newLevel + '.');
                    }
                }
            }.runTaskTimer(plugin, ticks, ticks);
        }
    }


    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (event.hasItem() && event.getItem().getType() == Material.SUGAR) {
                if (plugin.getPvpClassManager().getEquippedClass(event.getPlayer()) != this) {
                    return;
                }

                Player player = event.getPlayer();
                UUID uuid = player.getUniqueId();
                long timestamp = archerSpeedCooldowns.get(uuid);
                long millis = System.currentTimeMillis();
                long remaining = timestamp == archerSpeedCooldowns.getNoEntryValue() ? -1L : timestamp - millis;
                if (remaining > 0L) {
                    player.sendMessage(ChatColor.RED + "Cannot use " + getName() + " speed for another " + DurationFormatUtils.formatDurationWords(remaining, true, true) + ".");
                } else {
                    ItemStack stack = player.getItemInHand();
                    if (stack.getAmount() == 1) {
                        player.setItemInHand(new ItemStack(Material.AIR, 1));
                    } else {
                        stack.setAmount(stack.getAmount() - 1);
                    }

                    plugin.getEffectRestorer().setRestoreEffect(player, ARCHER_SPEED_EFFECT);
                    archerSpeedCooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis() + ARCHER_SPEED_COOLDOWN_DELAY);
                }
            }
        }
    }

    /*
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player
                && plugin.getPvpClassManager().getEquippedClass(player) == this
                && event.getEntity() instanceof FishHook hook) {
            player.teleport(player.getLocation().add(0, 1, 0));
            player.setVelocity(hook.getVelocity().multiply(0.8));
            hook.setBounce(false);
        }
    }
     */

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Vector vector3;
        Entity entity;
        Block block;
        Player player;
        double d;
        final double hookThreshold = 0.25;
        final double hForceMult = 0.75;
        final double hForceMax = 7.5;
        final double vForceMult = 0.7;
        final double vForceBonus = 0.5;
        final double vForceMax = 1.5;

        if (event.getState().equals(PlayerFishEvent.State.IN_GROUND) || event.getState().equals(PlayerFishEvent.State.FAILED_ATTEMPT)) {
            entity = event.getHook();
            block = entity.getWorld().getBlockAt(entity.getLocation().add(0.0, -hookThreshold, 0.0));

            if (!block.isEmpty() && !block.isLiquid()) {
                player = event.getPlayer();

                vector3 = entity.getLocation().subtract(player.getLocation()).toVector();

                if (vector3.getY() < 0.0)
                    vector3.setY(0.0);

                vector3.setX(vector3.getX() * hForceMult);
                vector3.setY(vector3.getY() * vForceMult + vForceBonus);
                vector3.setZ(vector3.getZ() * hForceMult);

                d = hForceMax * hForceMax;
                if (vector3.clone().setY(0.0).lengthSquared() > d) {
                    d = d / vector3.lengthSquared();
                    vector3.setX(vector3.getX() * d);
                    vector3.setZ(vector3.getZ() * d);
                }

                if (vector3.getY() > vForceMax)
                    vector3.setY(vForceMax);

                player.setVelocity(vector3);
            }
        }
    }

    @Override
    public boolean isApplicableFor(Player player) {
        PlayerInventory playerInventory = player.getInventory();

        ItemStack helmet = playerInventory.getHelmet();
        if (helmet == null || helmet.getType() != Material.LEATHER_HELMET) return false;

        ItemStack chestplate = playerInventory.getChestplate();
        if (chestplate == null || chestplate.getType() != Material.LEATHER_CHESTPLATE) return false;

        ItemStack leggings = playerInventory.getLeggings();
        if (leggings == null || leggings.getType() != Material.LEATHER_LEGGINGS) return false;

        ItemStack boots = playerInventory.getBoots();
        return !(boots == null || boots.getType() != Material.LEATHER_BOOTS);
    }
}
