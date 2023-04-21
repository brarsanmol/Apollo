package com.octavemc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.octavemc.util.BukkitUtils;
import com.octavemc.util.PersistableLocation;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public final class Configuration {

    public static final String PRIMARY_MESSAGE_PREFIX = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.AQUA + "" + ChatColor.BOLD + "!" + ChatColor.DARK_GRAY + "" + ChatColor.BOLD + ") ";

    public static final String WARNING_MESSAGE_PREFIX = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.YELLOW + "" + ChatColor.BOLD + "!" + ChatColor.DARK_GRAY + "" + ChatColor.BOLD + ") ";

    public static final String DANGER_MESSAGE_PREFIX = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.DARK_RED + "" + ChatColor.BOLD + "!" + ChatColor.DARK_GRAY + "" + ChatColor.BOLD + ") ";

    public static final boolean ALLOW_TELEPORTING_IN_ENEMY_TERRITORY = true;

    public static final boolean HANDLE_ENTITY_LIMITING = true;

    public static final boolean REMOVE_INFINITY_ARROWS_ON_LAND = true;

    public static final int BEACON_STRENGTH_LEVEL_LIMIT = 1;

    public static final boolean DISABLE_BOAT_PLACEMENT_ON_LAND = true;

    public static final boolean ENDERPEARL_GLITCHING_ENABLED = true;

    public static final boolean ENDERPEARL_GLITCHING_REFUND = true;

    public static final boolean DISABLE_ENDERCHESTS = true;

    public static final boolean PREVENT_PLACING_BEDS_IN_NETHER = false;

    public static final float FURNACE_COOK_SPEED_MULTIPLIER = 6.0F;

    public static final boolean BOTTLED_EXP = true;

    public static final boolean BOOKS_DISENCHANTING = true;

    public static final boolean DEATH_SIGNS = true;

    public static final boolean DEATH_LIGHTNING = true;

    public static final int MAP_NUMBER = 1;

    public static final boolean PREVENT_ALLY_ATTACK_DAMAGE = true;

    public static final int ECONOMY_STARTING_BALANCE = 250;

    public static final boolean SPAWNERS_PREVENT_BREAKING_NETHER = true;

    public static final boolean SPAWNERS_PREVENT_PLACING_NETHER = true;

    public static final float EXP_MULTIPLIER_GLOBAL = 1.0F;

    public static final float EXP_MULTIPLIER_FISHING = 1.0F;

    public static final float EXP_MULTIPLIER_SMELTING = 1.0F;

    public static final float EXP_MULTIPLIER_LOOTING_PER_LEVEL = 1.0F;

    public static final float EXP_MULTIPLIER_LUCK_PER_LEVEL = 1.0F;

    public static final float EXP_MULTIPLIER_FORTUNE_PER_LEVEL = 1.0F;

    public static final String SCOREBOARD_SIDEBAR_TITLE = "&b&lOctave &7(Season " + MAP_NUMBER + ")";

    public static final boolean SCOREBOARD_SIDEBAR_ENABLED = true;

    public static final boolean SCOREBOARD_NAMETAGS_ENABLED = true;

    public static final boolean HANDLE_COMBAT_LOGGING = true;

    public static final int COMBAT_LOG_DESPAWN_DELAY_TICKS = 600;

    public static final ImmutableMap<World.Environment, Integer> WARZONE_RADIUS = Maps.immutableEnumMap(ImmutableMap.of(
            World.Environment.NORMAL, 800,
            World.Environment.NETHER, 800
    ));

    public static final double WORLD_BORDER = 5000;

    public static final int WORLD_BORDER_WARNING_TIME = 3;

    public static final int WORLD_BORDER_WARNING_DISTANCE = 15;

    public static final int CONQUEST_POINT_LOSS_PER_DEATH = 20;

    public static final int CONQUEST_REQUIRED_VICTORY_POINTS = 300;

    public static final boolean CONQUEST_ALLOW_NEGATIVE_POINTS = true;

    public static final boolean ALLOW_CLAIMS_BESIDES_ROADS = true;

    public static final TreeSet<String> FACTION_DISSALLOWED_NAMES = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    public static final int MAX_HEIGHT_FACTION_HOME = -1;

    public static final char ECONOMY_SYMBOL = '$';

    public static final int FACTION_HOME_TELEPORT_DELAY_OVERWORLD_SECONDS = 10;
    public static final long FACTION_HOME_TELEPORT_DELAY_OVERWORLD_MILLIS = TimeUnit.SECONDS.toMillis(FACTION_HOME_TELEPORT_DELAY_OVERWORLD_SECONDS);

    public static final int FACTION_HOME_TELEPORT_DELAY_NETHER_SECONDS = 15;
    public static final long FACTION_HOME_TELEPORT_DELAY_NETHER_MILLIS = TimeUnit.SECONDS.toMillis(FACTION_HOME_TELEPORT_DELAY_NETHER_SECONDS);

    public static final int FACTION_HOME_TELEPORT_DELAY_END_SECONDS = 20;
    public static final long FACTION_HOME_TELEPORT_DELAY_END_MILLIS = TimeUnit.SECONDS.toMillis(FACTION_HOME_TELEPORT_DELAY_END_SECONDS);

    public static final int FACTION_NAME_MINIMUM_CHARACTERS = 3;

    public static final int FACTION_NAME_MAXIMUM_CHARACTERS = 16;

    public static final int FACTION_MAXIMUM_MEMBERS = 25;

    public static final int FACTION_MAXIMUM_CLAIMS = 8;

    public static final int FACTION_MAXIMUM_ALLIES = 1;

    public static final int FACTION_SUBCLAIM_NAME_MINIMUM_CHARACTERS = 3;

    public static final int FACTION_SUBCLAIM_NAME_MAXIMUM_CHARACTERS = 16;

    public static final int FACTION_DTR_REGEN_FREEZE_BASE_MINUTES = 40;
    public static final long FACTION_DTR_REGEN_FREEZE_BASE_MILLIS = TimeUnit.MINUTES.toMillis(FACTION_DTR_REGEN_FREEZE_BASE_MINUTES);

    public static final int FACTION_DTR_REGEN_FREEZE_MINUTES_PER_MEMBER = 2;
    public static final long FACTION_DTR_REGEN_FREEZE_MILLIS_PER_MEMBER = TimeUnit.MINUTES.toMillis(FACTION_DTR_REGEN_FREEZE_MINUTES_PER_MEMBER);

    public static final int FACTION_MINIMUM_DTR = -50;

    public static final float FACTION_MAXIMUM_DTR = 6.0F;

    public static final long FACTION_DTR_UPDATE_MILLIS = TimeUnit.SECONDS.toMillis(45);
    public static final String FACTION_DTR_UPDATE_TIME_WORDS = DurationFormatUtils.formatDurationWords(FACTION_DTR_UPDATE_MILLIS, true, true);

    public static final float FACTION_DTR_UPDATE_INCREMENT = 0.1F;

    public static final ChatColor RELATION_COLOUR_WARZONE = ChatColor.RED;

    public static final ChatColor RELATION_COLOUR_WILDERNESS = ChatColor.DARK_GREEN;

    public static final ChatColor RELATION_COLOUR_TEAMMATE = ChatColor.GREEN;

    public static final ChatColor RELATION_COLOUR_ALLY = ChatColor.DARK_AQUA;

    public static final ChatColor RELATION_COLOUR_ENEMY = ChatColor.RED;

    public static final ChatColor RELATION_COLOUR_ROAD = ChatColor.YELLOW;

    public static final ChatColor RELATION_COLOUR_SAFEZONE = ChatColor.AQUA;

    public static final int DEATHBAN_BASE_DURATION_MINUTES = 60;

    public static final int DEATHBAN_RESPAWN_SCREEN_SECONDS_BEFORE_KICK = 15;
    public static final int DEATHBAN_RESPAWN_SCREEN_TICKS_BEFORE_KICK = DEATHBAN_RESPAWN_SCREEN_SECONDS_BEFORE_KICK * 20;

    public static final boolean END_OPEN = true;

    public static final PersistableLocation END_EXIT_LOCATION = new PersistableLocation(Bukkit.getWorld("world"), 0.5, 75, 0.5);

    public static final boolean END_EXTINGUISH_FIRE_ON_EXIT = true;

    public static final boolean END_REMOVE_STRENGTH_ON_ENTRANCE = true;

    public static final List<String> POTION_LIMITS_UNSTORED = new ArrayList<>();

    public static final List<String> ENCHANTMENT_LIMITS_UNSTORED = new ArrayList<>();

    public static final boolean SUBCLAIM_SIGN_PRIVATE = false;

    public static final boolean SUBCLAIM_SIGN_CAPTAIN = false;

    public static final boolean SUBCLAIM_SIGN_LEADER = false;

    public static final boolean SUBCLAIM_HOPPER_CHECK = false;

    public static final int END_PORTAL_RADIUS = 20;

    public static final int END_PORTAL_CENTER = 500;

    private static final ImmutableMap<Enchantment, Integer> ENCHANTMENT_LIMITS = ImmutableMap.of(
            Enchantment.PROTECTION_ENVIRONMENTAL, 1,
            Enchantment.DAMAGE_ALL, 1
    );

    private static final ImmutableMap<PotionType, Integer> POTION_LIMITS = Maps.immutableEnumMap(ImmutableMap.of(
            PotionType.STRENGTH, 1
    ));

    public static final ImmutableMap<World.Environment, Integer> ROAD_LENGTHS = Maps.immutableEnumMap(ImmutableMap.of(
            World.Environment.NORMAL, 4000,
            World.Environment.NETHER, 4000
    ));

    public static final ImmutableMap<World.Environment, Integer> SPAWN_RADIUS = Maps.immutableEnumMap(ImmutableMap.of(
            World.Environment.NORMAL, 49,
            World.Environment.NETHER, 25,
            World.Environment.THE_END, 15
    ));

    public static final int STACKING_RADIUS = 10;

    public static final Set<EntityType> STACKABLE_ENTITIES = ImmutableSet.of(
            EntityType.COW,
            EntityType.PIG,
            EntityType.CHICKEN,
            EntityType.RABBIT,
            EntityType.SQUID,
            EntityType.CREEPER,
            EntityType.ZOMBIE,
            EntityType.PIG_ZOMBIE,
            EntityType.SKELETON,
            EntityType.SPIDER,
            EntityType.CAVE_SPIDER,
            EntityType.BLAZE,
            EntityType.ENDERMAN
    );

    public static final String TEXTURE_DATA = "eyJ0aW1lc3RhbXAiOjE0MTEyNjg3OTI3NjUsInByb2ZpbGVJZCI6IjNmYmVjN2RkMGE1ZjQwYmY5ZDExODg1YTU0NTA3MTEyIiwicHJvZmlsZU5hbWUiOiJsYXN0X3VzZXJuYW1lIiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg0N2I1Mjc5OTg0NjUxNTRhZDZjMjM4YTFlM2MyZGQzZTMyOTY1MzUyZTNhNjRmMzZlMTZhOTQwNWFiOCJ9fX0=\", \"u8sG8tlbmiekrfAdQjy4nXIcCfNdnUZzXSx9BE1X5K27NiUvE1dDNIeBBSPdZzQG1kHGijuokuHPdNi/KXHZkQM7OJ4aCu5JiUoOY28uz3wZhW4D+KG3dH4ei5ww2KwvjcqVL7LFKfr/ONU5Hvi7MIIty1eKpoGDYpWj3WjnbN4ye5Zo88I2ZEkP1wBw2eDDN4P3YEDYTumQndcbXFPuRRTntoGdZq3N5EBKfDZxlw4L3pgkcSLU5rWkd5UH4ZUOHAP/VaJ04mpFLsFXzzdU4xNZ5fthCwxwVBNLtHRWO26k/qcVBzvEXtKGFJmxfLGCzXScET/OjUBak/JEkkRG2m+kpmBMgFRNtjyZgQ1w08U6HHnLTiAiio3JswPlW5v56pGWRHQT5XWSkfnrXDalxtSmPnB5LmacpIImKgL8V9wLnWvBzI7SHjlyQbbgd+kUOkLlu7+717ySDEJwsFJekfuR6N/rpcYgNZYrxDwe4w57uDPlwNL6cJPfNUHV7WEbIU1pMgxsxaXe8WSvV87qLsR7H06xocl2C0JFfe2jZR4Zh3k9xzEnfCeFKBgGb4lrOWBu1eDWYgtKV67M2Y+B3W5pjuAjwAxn0waODtEn/3jKPbc/sxbPvljUCw65X+ok0UUN1eOwXV5l2EGzn05t3Yhwq19/GxARg63ISGE8CKw=";

    public static final String TEXTURE_SIGNATURE = "C9I8+HnqzpUhwQCbaWDNOloTrj64yafoXwUx3WmjOE13xxfktXEXMilRHpTV9Wu4iye6SRDdYHFY23f8+2JWjZQLF9oTZzZpGAH/mpVdtioAqNXEI5vuF9IuSbdnI9AXkYag0A6cu5nZEOCRqBRDbXKWXZmOBpaq44uptD2CBSNMXvV1cBZ4bba1T0lWKyWHrirh+5Tll1TtGdb42gKVoAkYqNpYQ71CASZPoMNN3Enqp+grHPhYwMlvYXqRcQF3vXbFoqbV6g/iu4pdUOTrBz+e78CZfephaAIMD3tPLbVkGkmSDen1Wcfs0sF8G9v4a3Wf10CZEscflTA7I36XX1pZ4edlOOe5rHHJ5TFGrPR74lHmahwz+RG0ObG6zu9tE+Xu11KaBv4SmeOdQ98JI+z/QbC/IbMQ5tzkHlsjr6VJR+mheNipl/EImDjmz4exFLZDXhq/0U6+QBVZ7YTQv9rMKS9TFyGjMlg8IOu8hhhlRaLdrKksXiFvEqp4tx7aPdHiypqUnMlg/Ri5cpxFJaCGkARk1HjdAkUyFTtlcLos1dMyMpz3ltnBRp1SurTeO6CL2DJR1WT6f6U/KifRhI9+PljT0wDeU7vhbgPaLs+r5/pBKKXALez+T/GucJdk0rZDVMDYHK+MzgEVijIxfjC+dDbN9oiQKYODGiQTFss=";

    public static final String TEXTURE_URL = "http://textures.minecraft.net/texture/231e7dad704126a67288c4446aa5461a451d8c7623c7ac0cda1c2bc51093bb89";

    public static int getEnchantmentLimit(Enchantment enchantment) {
        return ENCHANTMENT_LIMITS.getOrDefault(enchantment, enchantment.getMaxLevel());
    }

    public static int getPotionLimit(PotionType potionEffectType) {
        return POTION_LIMITS.getOrDefault(potionEffectType, potionEffectType.getMaxLevel());
    }

    public static final ImmutableList<String[]> BROADCASTS = ImmutableList.<String[]>builder()
            .add(new String[] {
            ChatColor.WHITE + BukkitUtils.STRAIGHT_LINE_DEFAULT,
            ChatColor.AQUA + "" + ChatColor.ITALIC + " Discord",
            " ",
            ChatColor.GRAY + " Need support? Looking for a faction, or want to connect with our community? Join our discord...",
            ChatColor.AQUA + " https://discord.octavemc.com",
            ChatColor.WHITE + BukkitUtils.STRAIGHT_LINE_DEFAULT
            })
            .add(new String[] {
                    ChatColor.WHITE + BukkitUtils.STRAIGHT_LINE_DEFAULT,
                    ChatColor.AQUA + "" + ChatColor.ITALIC + " Shop",
                    " ",
                    ChatColor.GRAY + " Help support the server by purchasing ranks, crates, and lives at",
                    ChatColor.AQUA + " https://shop.octavemc.com/",
                    ChatColor.WHITE + BukkitUtils.STRAIGHT_LINE_DEFAULT
            })
            .add(new String[] {
                    ChatColor.WHITE + BukkitUtils.STRAIGHT_LINE_DEFAULT,
                    ChatColor.AQUA + "" + ChatColor.ITALIC + " Twitter",
                    " ",
                    ChatColor.GRAY + " Don't miss out! Follow our twitter for important information, and sneakpeeks on our newest features.",
                    ChatColor.AQUA + " https://twitter.octavemc.com",
                    ChatColor.WHITE + BukkitUtils.STRAIGHT_LINE_DEFAULT
            }).build();
}