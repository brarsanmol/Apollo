package com.octavemc.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import gnu.trove.list.TCharList;
import gnu.trove.list.array.TCharArrayList;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.ChatPaginator;

/**
 * Utility class for simplifying tasks in the Bukkit API.
 */
@UtilityClass
public final class BukkitUtils {

    private static final ImmutableMap<ChatColor, DyeColor> CHAT_DYE_COLOUR_MAP;
    private static final ImmutableSet<PotionEffectType> DEBUFF_TYPES;
    private static final TCharList COLOUR_CHARACTER_LIST;

    /**
     * Internal use only
     */
    private static final String STRAIGHT_LINE_TEMPLATE;

    /**
     * The default straight line string wrapped across the Minecraft font width.
     */
    public static final String STRAIGHT_LINE_DEFAULT;

    static {
        STRAIGHT_LINE_TEMPLATE = ChatColor.STRIKETHROUGH + Strings.repeat("-", 256);
        STRAIGHT_LINE_DEFAULT = STRAIGHT_LINE_TEMPLATE.substring(0, ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH);

        CHAT_DYE_COLOUR_MAP = /*TODO:Maps.immutableEnumMap*/(ImmutableMap.<ChatColor, DyeColor>builder().
                put(ChatColor.AQUA, DyeColor.LIGHT_BLUE).
                put(ChatColor.BLACK, DyeColor.BLACK).
                put(ChatColor.BLUE, DyeColor.LIGHT_BLUE).
                put(ChatColor.DARK_AQUA, DyeColor.CYAN).
                put(ChatColor.DARK_BLUE, DyeColor.BLUE).
                put(ChatColor.DARK_GRAY, DyeColor.GRAY).
                put(ChatColor.DARK_GREEN, DyeColor.GREEN).
                put(ChatColor.DARK_PURPLE, DyeColor.PURPLE).
                put(ChatColor.DARK_RED, DyeColor.RED).
                put(ChatColor.GOLD, DyeColor.ORANGE).
                put(ChatColor.GRAY, DyeColor.SILVER).
                put(ChatColor.GREEN, DyeColor.LIME).
                put(ChatColor.LIGHT_PURPLE, DyeColor.MAGENTA).
                put(ChatColor.RED, DyeColor.RED).
                put(ChatColor.WHITE, DyeColor.WHITE).
                put(ChatColor.YELLOW, DyeColor.YELLOW).build());

        DEBUFF_TYPES = ImmutableSet.<PotionEffectType>builder().
                add(PotionEffectType.BLINDNESS).
                add(PotionEffectType.CONFUSION).
                add(PotionEffectType.HARM).
                add(PotionEffectType.HUNGER).
                add(PotionEffectType.POISON).
                add(PotionEffectType.SATURATION).
                add(PotionEffectType.SLOW).
                add(PotionEffectType.SLOW_DIGGING).
                add(PotionEffectType.WEAKNESS).
                add(PotionEffectType.WITHER).build();

        COLOUR_CHARACTER_LIST = new TCharArrayList(ChatColor.values().length);
        for (ChatColor colour : ChatColor.values()) {
            COLOUR_CHARACTER_LIST.add(colour.getChar());
        }
    }

    static {
    }

    /**
     * Gets the time in milliseconds a {@link Player} has been idle for
     *
     * @param player the {@link Player} to get for
     * @return the time in milliseconds
     */
    public static long getIdleTime(Player player) {
        Preconditions.checkNotNull(player);
        long idleTime = ((CraftPlayer) player).getHandle().D();
        return idleTime > 0L ? MinecraftServer.az() - idleTime : 0L;
    }

    /**
     * Converts an {@link ChatColor} to a {@link DyeColor}.
     *
     * @param colour the {@link ChatColor} to be converted
     * @return the converted colour.
     */
    public static DyeColor toDyeColor(ChatColor colour) {
        return CHAT_DYE_COLOUR_MAP.get(colour);
    }

    /**
     * Gets the final {@link Player} attacker from the {@link EntityDamageEvent} including
     * {@link org.bukkit.projectiles.ProjectileSource} usage and everything else.
     *
     * @param event the {@link EntityDamageEvent} to get for
     * @param ignoreSelf if should ignore if the {@link Player} attacked self
     * @return the {@link Player} attacker of the event
     */
    public static Player getFinalAttacker(EntityDamageEvent event, boolean ignoreSelf) {
        Player attacker = null;
        if (event instanceof EntityDamageByEntityEvent damageEvent) {
            Entity damager = damageEvent.getDamager();
            if (damageEvent.getDamager() instanceof Player) {
                attacker = (Player) damager;
            } else if (damageEvent.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
                attacker = shooter;
            }
            if (attacker != null && ignoreSelf && event.getEntity().equals(attacker)) {
                attacker = null;
            }
        }

        return attacker;
    }

    /**
     * Checks if a {@link Location} is within a specific distance of another {@link Location}.
     *
     * @param location the location to check for {@link Location}
     * @param other    the other {@link Location}
     * @param distance the distance to check for
     * @return true if the {@link Location} is within the distance
     */
    public static boolean isWithinX(Location location, Location other, double distance) {
        return location.getWorld().equals(other.getWorld()) &&
                Math.abs(other.getX() - location.getX()) <= distance && Math.abs(other.getZ() - location.getZ()) <= distance;
    }

    /**
     * Gets the highest {@link Location} at another specified {@link Location}.
     *
     * @param origin the {@link Location} the location to find at
     * @return the highest {@link Location} from origin
     */
    public static Location getHighestLocation(Location origin) {
        return getHighestLocation(origin, null);
    }

    /**
     * Gets the highest {@link Location} at another specified {@link Location}.
     *
     * @param origin the {@link Location} the location to find at
     * @param def    the default {@link Location} if not found
     * @return the highest {@link Location} from origin
     */
    public static Location getHighestLocation(@NonNull Location origin, Location def) {
        Location cloned = origin.clone();
        World world = cloned.getWorld();
        int x = cloned.getBlockX();
        int y = world.getMaxHeight();
        int z = cloned.getBlockZ();
        while (y > origin.getBlockY()) {
            Block block = world.getBlockAt(x, --y, z);
            if (!block.isEmpty()) {
                Location next = block.getLocation();
                next.setPitch(origin.getPitch());
                next.setYaw(origin.getYaw());
                return next;
            }
        }

        return def;
    }

    /**
     * Checks if a {@link PotionEffectType} is a debuff.
     *
     * @param type the {@link PotionEffectType} to check
     * @return true if the {@link PotionEffectType} is a debuff
     */
    public static boolean isDebuff(PotionEffectType type) {
        return DEBUFF_TYPES.contains(type);
    }

    /**
     * Checks if a {@link PotionEffect} is a debuff.
     *
     * @param potionEffect the {@link PotionEffect} to check
     * @return true if the {@link PotionEffect} is a debuff
     */
    public static boolean isDebuff(PotionEffect potionEffect) {
        return isDebuff(potionEffect.getType());
    }

    /**
     * Checks if a {@link ThrownPotion} is a debuff.
     *
     * @param thrownPotion the {@link ThrownPotion} to check
     * @return true if the {@link ThrownPotion} is a debuff
     */
    public static boolean isDebuff(ThrownPotion thrownPotion) {
        for (PotionEffect effect : thrownPotion.getEffects()) {
            if (isDebuff(effect)) {
                return true;
            }
        }

        return false;
    }

    public static String getCardinalDirection(double bearing) {
        double rotation = (bearing - 180) % 360;
        if (rotation < 0) rotation += 360.0;
        if (0 <= rotation && rotation < 22.5) return "N";
        else if (22.5 <= rotation && rotation < 67.5) return "NE";
        else if (67.5 <= rotation && rotation < 112.5) return "E";
        else if (112.5 <= rotation && rotation < 157.5) return "SE";
        else if (157.5 <= rotation && rotation < 202.5) return "S";
        else if (202.5 <= rotation && rotation < 247.5) return "SW";
        else if (247.5 <= rotation && rotation < 292.5) return "W";
        else if (292.5 <= rotation && rotation < 337.5) return "NW";
        else if (337.5 <= rotation && rotation < 360.0) return "N";
        else return "Unknown";
    }

    public static ArmorStand createHologram(Location location, String text) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setGravity(false);
        stand.setCanPickupItems(false);
        stand.setCustomName(text);
        stand.setCustomNameVisible(true);
        stand.setVisible(false);
        return stand;
    }

}
