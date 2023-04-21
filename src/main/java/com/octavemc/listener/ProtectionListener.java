package com.octavemc.listener;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.eventgame.CaptureZone;
import com.octavemc.eventgame.faction.CapturableFaction;
import com.octavemc.faction.claim.Claim;
import com.octavemc.faction.event.CaptureZoneEnterEvent;
import com.octavemc.faction.event.CaptureZoneLeaveEvent;
import com.octavemc.faction.event.PlayerClaimEnterEvent;
import com.octavemc.faction.struct.Raidable;
import com.octavemc.faction.struct.Role;
import com.octavemc.faction.type.ClaimableFaction;
import com.octavemc.faction.type.Faction;
import com.octavemc.faction.type.PlayerFaction;
import com.octavemc.faction.type.WarzoneFaction;
import com.octavemc.util.BukkitUtils;
import com.octavemc.util.cuboid.Cuboid;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.material.Cauldron;
import org.bukkit.material.MaterialData;
import org.bukkit.projectiles.ProjectileSource;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Listener that manages protection for {@link Claim}s.
 */
public class ProtectionListener implements Listener {

    public static final String PROTECTION_BYPASS_PERMISSION = "apollo.faction.protection.bypass";

    // List of materials a player can not right click in enemy territory. ~No such ImmutableEnumMultimap in current Guava build :/
    //TODO: Put this in the configuration class
    private static final ImmutableMultimap<Material, Material> ITEM_ON_BLOCK_RIGHT_CLICK_DENY = ImmutableMultimap.<Material, Material>builder().
            put(Material.DIAMOND_HOE, Material.GRASS).
            put(Material.GOLD_HOE, Material.GRASS).
            put(Material.IRON_HOE, Material.GRASS).
            put(Material.STONE_HOE, Material.GRASS).
            put(Material.WOOD_HOE, Material.GRASS).
            build();

    // List of materials a player can not right click in enemy territory.
    //TODO: Put this in the configuration class
    private static final ImmutableSet<Material> BLOCK_RIGHT_CLICK_DENY = Sets.immutableEnumSet(
            Material.BED,
            Material.BED_BLOCK,
            Material.BEACON,
            Material.FENCE_GATE,
            Material.IRON_DOOR,
            Material.TRAP_DOOR,
            Material.WOOD_DOOR,
            Material.WOODEN_DOOR,
            Material.IRON_DOOR_BLOCK,
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.FURNACE,
            Material.BURNING_FURNACE,
            Material.BREWING_STAND,
            Material.HOPPER,
            Material.DROPPER,
            Material.DISPENSER,
            Material.STONE_BUTTON,
            Material.WOOD_BUTTON,
            Material.ENCHANTMENT_TABLE,
            Material.WORKBENCH,
            Material.ANVIL,
            Material.LEVER,
            Material.FIRE
    );

    private final Apollo plugin;

    public ProtectionListener(Apollo plugin) {
        this.plugin = plugin;
    }

    private void handleMove(PlayerMoveEvent event, PlayerClaimEnterEvent.EnterCause enterCause) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        boolean cancelled = false;

        Faction fromFaction = plugin.getFactionDao().getFactionAt(from).get();
        Faction toFaction = plugin.getFactionDao().getFactionAt(to).get();
        if (fromFaction != toFaction) {
            PlayerClaimEnterEvent calledEvent = new PlayerClaimEnterEvent(player, from, to, fromFaction, toFaction, enterCause);
            Bukkit.getPluginManager().callEvent(calledEvent);
            cancelled = calledEvent.isCancelled();
        } else if (toFaction instanceof CapturableFaction) {
            CapturableFaction capturableFaction = (CapturableFaction) toFaction;
            for (CaptureZone captureZone : capturableFaction.getCaptureZones()) {
                Cuboid cuboid = captureZone.getCuboid();
                if (cuboid == null) {
                    continue;
                }

                if (cuboid.contains(from)) {
                    if (!cuboid.contains(to)) {
                        CaptureZoneLeaveEvent calledEvent = new CaptureZoneLeaveEvent(player, capturableFaction, captureZone);
                        Bukkit.getPluginManager().callEvent(calledEvent);
                        cancelled = calledEvent.isCancelled();
                        break;
                    }
                } else {
                    if (cuboid.contains(to)) {
                        CaptureZoneEnterEvent calledEvent = new CaptureZoneEnterEvent(player, capturableFaction, captureZone);
                        Bukkit.getPluginManager().callEvent(calledEvent);
                        cancelled = calledEvent.isCancelled();
                        break;
                    }
                }
            }
        }

        if (cancelled) {
            if (enterCause == PlayerClaimEnterEvent.EnterCause.TELEPORT) {
                event.setCancelled(true);
            } else {
                from.setX(from.getBlockX() + 0.5);
                from.setZ(from.getBlockZ() + 0.5);
                event.setTo(from);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        this.handleMove(event, PlayerClaimEnterEvent.EnterCause.MOVEMENT);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerTeleportEvent event) {
        this.handleMove(event, PlayerClaimEnterEvent.EnterCause.TELEPORT);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        switch (event.getCause()) {
            case FLINT_AND_STEEL:
            case ENDER_CRYSTAL:
                return;
        }

        Apollo.getInstance().getFactionDao().getFactionAt(event.getBlock().getLocation())
                .filter(faction -> faction instanceof ClaimableFaction && !(faction instanceof PlayerFaction))
                .ifPresent(faction -> event.setCancelled(true));
        /*
        Faction faction = Apollo.getInstance().getFactionDao().getFactionAt(event.getBlock().getLocation()).get();
        if (faction instanceof ClaimableFaction && !(faction instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
        */
    }

    // Original source by mFactions: https://github.com/MassiveCraft/Factions/blob/dab81ede383aeb76606daf5a3c859775e1b3778/src/com/massivecraft/factions/engine/EngineExploit.java
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onStickyPistonExtend(BlockPistonExtendEvent event) {
        Block block = event.getBlock();

        // Targets end-of-the-line empty (AIR) block which is being pushed into, including if piston itself would extend into air.
        Block targetBlock = block.getRelative(event.getDirection(), event.getLength() + 1);
        if (targetBlock.isEmpty() || targetBlock.isLiquid()) { // If potentially pushing into AIR/WATER/LAVA in another territory, check it out.
            Faction targetFaction = plugin.getFactionDao().getFactionAt(targetBlock.getLocation()).get();
            if (targetFaction instanceof Raidable && !((Raidable) targetFaction).isRaidable() && targetFaction != plugin.getFactionDao().getFactionAt(block.getLocation()).get()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onStickyPistonRetract(BlockPistonRetractEvent event) {
        if (!event.isSticky()) return; // If not a sticky piston, retraction should be fine.

        // If potentially retracted block is just AIR/WATER/LAVA, no worries
        Location retractLocation = event.getRetractLocation();
        Block retractBlock = retractLocation.getBlock();
        if (!retractBlock.isEmpty() && !retractBlock.isLiquid()) {
            Block block = event.getBlock();
            Faction targetFaction = plugin.getFactionDao().getFactionAt(retractLocation).get();
            if (targetFaction instanceof Raidable && !((Raidable) targetFaction).isRaidable() && targetFaction != plugin.getFactionDao().getFactionAt(block.getLocation()).get()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockFromTo(BlockFromToEvent event) {
        Block fromBlock = event.getBlock();
        Material fromType = fromBlock.getType();
        if (fromType == Material.WATER || fromType == Material.STATIONARY_WATER || fromType == Material.LAVA || fromType == Material.STATIONARY_LAVA) {
            if (!ProtectionListener.canBuildAt(fromBlock.getLocation(), event.getToBlock().getLocation())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Apollo.getInstance().getFactionDao().getFactionAt(event.getTo())
                    .filter(faction -> faction.isSafezone() && Apollo.getInstance().getFactionDao().getFactionAt(event.getFrom()).get().isSafezone())
                    .ifPresent(faction -> {
                        event.getPlayer().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + "You threw a enderpearl into a safe-zone, it has been refunded.");
                        Apollo.getInstance().getTimerManager().getEnderpearlTimer().refund(event.getPlayer());
                        event.setCancelled(true);
                    });
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            Location from = event.getFrom();
            Location to = event.getTo();
            Player player = event.getPlayer();

            Faction fromFac = plugin.getFactionDao().getFactionAt(from).get();
            if (fromFac.isSafezone()) { // teleport player to spawn point of target if came from safe-zone.
                event.setTo(to.getWorld().getSpawnLocation().add(0.5, 0, 0.5));
                event.useTravelAgent(false);
                player.sendMessage(ChatColor.YELLOW + "You were teleported to the spawn of target world as you were in a safe-zone.");
                return;
            }

            if (event.useTravelAgent() && to.getWorld().getEnvironment() == World.Environment.NORMAL) {
                if (!event.getPortalTravelAgent().getCanCreatePortal()
                        || event.getPortalTravelAgent().findPortal(event.getTo()) != null) return;
                Apollo.getInstance().getFactionDao().getFactionAt(to)
                        .filter(at -> at instanceof ClaimableFaction)
                        .ifPresent(at ->
                            Apollo.getInstance().getFactionDao().getByPlayer(event.getPlayer().getUniqueId())
                                    .filter(faction -> faction != at)
                                    .ifPresent(faction -> {
                                player.sendMessage(ChatColor.YELLOW + "Portal would have created portal in territory of " + at.getDisplayName(player) + ChatColor.YELLOW + '.');
                                event.setCancelled(true);
                            })
                        );
            }
        }
    }

    // Prevent mobs from spawning in the Warzone, safe-zones or claims.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Apollo.getInstance().getFactionDao().getFactionAt(event.getLocation())
                .filter(faction -> faction.isDeathban()
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SLIME_SPLIT
                && faction instanceof ClaimableFaction
                && ((!(faction instanceof Raidable) || !((Raidable) faction).isRaidable()))
                && event.getEntity() instanceof Monster)
                .ifPresent(faction -> {
                    switch (event.getSpawnReason()) {
                        case SPAWNER:
                        case EGG:
                        case CUSTOM:
                        case BUILD_WITHER:
                        case BUILD_IRONGOLEM:
                        case BUILD_SNOWMAN:
                            return;
                        default:
                            event.setCancelled(true);
                    }
                });
    }

    // Prevents players attacking or taking damage when in safe-zone protected areas.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            Faction playerFactionAt = plugin.getFactionDao().getFactionAt(player.getLocation()).get();
            EntityDamageEvent.DamageCause cause = event.getCause();
            if (playerFactionAt.isSafezone() && cause != EntityDamageEvent.DamageCause.SUICIDE && cause != EntityDamageEvent.DamageCause.VOID) {
                event.setCancelled(true);
            }

            Player attacker = BukkitUtils.getFinalAttacker(event, true);
            if (attacker != null) {
                Faction attackerFactionAt = plugin.getFactionDao().getFactionAt(attacker.getLocation()).get();
                if (attackerFactionAt.isSafezone()) {
                    event.setCancelled(true);
                    attacker.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not damage players while in a safe-zone.");
                    return;
                } else if (playerFactionAt.isSafezone()) {
                    // it's already cancelled above.
                    attacker.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not damage players while in a safe-zone.");
                    return;
                }

                Optional<PlayerFaction> attackerFaction;
                Optional<PlayerFaction> playerFaction = plugin.getFactionDao().getByPlayer(player.getUniqueId());
                if (playerFaction.isPresent() && ((attackerFaction = plugin.getFactionDao().getByPlayer(attacker.getUniqueId())).isPresent())) {
                    Role role = playerFaction.get().getMember(player).getRole();
                    if (attackerFaction.get() == playerFaction.get()) {
                        attacker.sendMessage(Configuration.WARNING_MESSAGE_PREFIX + Configuration.RELATION_COLOUR_TEAMMATE + role.getAstrix() + player.getName() + ChatColor.GRAY + " is in your faction.");
                        event.setCancelled(true);
                    } else if (attackerFaction.get().getAllied().contains(playerFaction.get().getIdentifier())) {
                        ChatColor color = Configuration.RELATION_COLOUR_ALLY;
                        // if (plugin.getConfiguration().isPreventAllyAttackDamage()) {
                        //    event.setCancelled(true);
                        //    attacker.sendMessage(color + hiddenAstrixedName + ChatColor.YELLOW + " is an ally.");
                        // } else {
                        attacker.sendMessage(Configuration.WARNING_MESSAGE_PREFIX + Configuration.RELATION_COLOUR_TEAMMATE + role.getAstrix() + player.getName() + ChatColor.GRAY + " is an ally.");
                        // }
                    }
                }
            }
        }
    }

    // Prevents losing hunger in safe-zones.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player && ((Player) entity).getFoodLevel() > event.getFoodLevel() && plugin.getFactionDao().getFactionAt(entity.getLocation()).get().isSafezone()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getEntity();
        if (!BukkitUtils.isDebuff(potion)) {
            return;
        }

        // Prevents potion effecting players that are in safe-zones.
        Faction factionAt = plugin.getFactionDao().getFactionAt(potion.getLocation()).get();
        if (factionAt.isSafezone()) {
            event.setCancelled(true);
            return;
        }

        ProjectileSource source = potion.getShooter();
        if (source instanceof Player) {
            Player player = (Player) source;
            //Allow faction members to splash damage their own, PlayerFaction playerFaction = plugin.getFactionDao().getPlayerFaction(player);
            for (LivingEntity affected : event.getAffectedEntities()) {
                if (affected instanceof Player && !player.equals(affected)) {
                    Player target = (Player) affected;
                    if (target.equals(source)) continue; // allow the source to be affected regardless
                    if (plugin.getFactionDao().getFactionAt(target.getLocation()).get().isSafezone()/*Nope || playerFaction.getMembers().containsKey(other.getUniqueId())*/) {
                        event.setIntensity(affected, 0);
                    }
                }
            }
        }
    }

    // Prevent monsters targeting players in safe-zones or their own claims.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event) {
        switch (event.getReason()) {
            case CLOSEST_PLAYER:
            case RANDOM_TARGET:
                Entity target = event.getTarget();
                if (event.getEntity() instanceof LivingEntity && target instanceof Player) {
                    // Check LivingEntity instance, things like experience orbs might lag spam ;/
                    Optional<PlayerFaction> playerFaction = plugin.getFactionDao().getByPlayer(target.getUniqueId());
                    Faction factionAt = plugin.getFactionDao().getFactionAt(target.getLocation()).get();
                    if (factionAt.isSafezone() || playerFaction.isPresent() && factionAt == playerFaction.get()) {
                        event.setCancelled(true);
                    }
                }
                break;
            default:
                break;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) {
            return;
        }

        Block block = event.getClickedBlock();
        Action action = event.getAction();
        if (action == Action.PHYSICAL) { // Prevent players from trampling on crops or pressure plates, etc.
            if (!attemptBuild(event.getPlayer(), block.getLocation(), null)) {
                event.setCancelled(true);
            }
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            boolean canRightClick;
            MaterialData blockData;
            Material blockType = block.getType();

            // Firstly, check if this block is not on the explicit blacklist.
            canRightClick = !BLOCK_RIGHT_CLICK_DENY.contains(blockType);
            if (canRightClick) {
                Material itemType = event.hasItem() ? event.getItem().getType() : null;

                if (Material.EYE_OF_ENDER == itemType && Material.ENDER_PORTAL_FRAME == blockType && block.getData() != 4) {
                    // If the player is right clicking an Ender Portal Frame with an Ender Portal Eye and it is empty.
                    canRightClick = false;

                } else if (Material.GLASS_BOTTLE == itemType && (blockData = block.getState().getData()) instanceof Cauldron && !((Cauldron) blockData).isEmpty()) {
                    // If the player is right clicking a Cauldron that contains liquid with a Glass Bottle.
                    canRightClick = false;

                } else if (itemType != null && ITEM_ON_BLOCK_RIGHT_CLICK_DENY.get(itemType).contains(block.getType())) {
                    // Finally, check if this block is not blacklisted with the item the player right clicked it with.
                    canRightClick = false;

                }
            } else if (block.getType() == Material.WORKBENCH && plugin.getFactionDao().getFactionAt(block.getLocation()).get().isSafezone()) {
                // Allow workbench use in safezones.
                canRightClick = true;
            }

            if (!canRightClick && !attemptBuild(event.getPlayer(), block.getLocation(), Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You cannot do this in the territory of %1$s" + ChatColor.GRAY + '.', true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBurn(BlockBurnEvent event) {
        Faction factionAt = plugin.getFactionDao().getFactionAt(event.getBlock().getLocation()).get();
        if (factionAt instanceof WarzoneFaction || (factionAt instanceof Raidable && !((Raidable) factionAt).isRaidable())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockFade(BlockFadeEvent event) {
        Faction factionAt = plugin.getFactionDao().getFactionAt(event.getBlock().getLocation()).get();
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }

    /*@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockSpread(BlockSpreadEvent event) {
        Faction factionAt = plugin.getFactionDao().getFactionAt(event.getBlock().getLocation());
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }*/

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onLeavesDelay(LeavesDecayEvent event) {
        Faction factionAt = plugin.getFactionDao().getFactionAt(event.getBlock().getLocation()).get();
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockForm(BlockFormEvent event) {
        Faction factionAt = plugin.getFactionDao().getFactionAt(event.getBlock().getLocation()).get();
        if (factionAt instanceof ClaimableFaction && !(factionAt instanceof PlayerFaction)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity && !attemptBuild(entity, event.getBlock().getLocation(), null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!attemptBuild(event.getPlayer(), event.getBlock().getLocation(), Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not build in the territory of %1$s" + ChatColor.GRAY + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!attemptBuild(event.getPlayer(), event.getBlockPlaced().getLocation(), Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not build in the territory of %1$s" + ChatColor.GRAY + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!attemptBuild(event.getPlayer(), event.getBlockClicked().getLocation(), Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not build in the territory of %1$s" + ChatColor.GRAY + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!attemptBuild(event.getPlayer(), event.getBlockClicked().getLocation(), Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You cannot build in the territory of %1$s" + ChatColor.GRAY + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        if (remover instanceof Player) {
            if (!attemptBuild(remover, event.getEntity().getLocation(), Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not build in the territory of %1$s" + ChatColor.GRAY + '.')) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!attemptBuild(event.getPlayer(), event.getEntity().getLocation(), Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not build in the territory of %1$s" + ChatColor.GRAY + '.')) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player) {
            if (!attemptBuild(event.getEntered(), event.getVehicle().getLocation(), Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not enter vehicles in the territory of %1$s" + ChatColor.GRAY + '.')) {
                event.setCancelled(true);
            } else if (event.getVehicle() instanceof Horse horse
                    && horse.getOwner() != null
                    && !horse.getOwner().equals(event.getVehicle())) {
                // Prevent players using horses that don't belong to them.
                event.getEntered().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + "You cannot enter a horse that belongs to " + ChatColor.AQUA + horse.getOwner().getName() + ChatColor.GRAY + '.');
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleDamage(VehicleDamageEvent event) {
        Player damager = null;
        Entity attacker = event.getAttacker();
        if (attacker instanceof Player) {
            damager = (Player) attacker;
        } else if (attacker instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) attacker).getShooter();
            if (shooter instanceof Player) {
                damager = (Player) shooter;
            }
        }

        if (damager != null && !attemptBuild(attacker, damager.getLocation(), Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You cannot build in the territory of %1$s" + ChatColor.GRAY + '.')) {
            event.setCancelled(true);
        }
    }

    // Prevents items that are in Item Frames OR hanging entities (PAINTINGS, etc) being removed.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onHangingDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Hanging) {
            Player attacker = BukkitUtils.getFinalAttacker(event, false);
            if (attacker != null && !attemptBuild(attacker, entity.getLocation(), Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You cannot build in the territory of %1$s" + ChatColor.GRAY + '.')) {
                event.setCancelled(true);
            }
        }
    }

    // Prevents items that are in Item Frames being rotated.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onHangingInteractByPlayer(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity instanceof Hanging) {
            if (!attemptBuild(event.getPlayer(), entity.getLocation(), Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You cannot build in the territory of %1$s" + ChatColor.GRAY + '.')) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Checks if a entity is eligible to build at a given location, if not
     * it will send the deny message passed in the constructor.
     * <p>The deny message will be formatted using {@link String#format(String, Object...)}</p>
     * <p>The first formatted argument is the display name of the enemy faction to the player</p>
     *
     * @param entity      the entity to attempt for
     * @param location    the location to attempt at
     * @param denyMessage the deny message to send
     * @return true if the player can build at location
     */
    public static boolean attemptBuild(Entity entity, Location location, @Nullable String denyMessage) {
        return attemptBuild(entity, location, denyMessage, false);
    }

    /**
     * Checks if a entity is eligible to build at a given location, if not
     * it will send the deny message passed in the constructor.
     * <p>The deny message will be formatted using {@link String#format(String, Object...)}</p>
     * <p>The first formatted argument is the display name of the enemy faction to the player</p>
     *
     * @param entity        the entity to attempt for
     * @param location      the location to attempt at
     * @param denyMessage   the deny message to send
     * @param isInteraction if the entity is trying to interact
     * @return true if the player can build at location
     */
    public static boolean attemptBuild(Entity entity, Location location, @Nullable String denyMessage, boolean isInteraction) {
        Player player = entity instanceof Player ? (Player) entity : null;

        // Allow CREATIVE players with specified permission to bypass this protection.
        if (player != null && player.getGameMode() == GameMode.CREATIVE && player.hasPermission(PROTECTION_BYPASS_PERMISSION)) {
            return true;
        }

        if (player != null && player.getWorld().getEnvironment() == World.Environment.THE_END) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You cannot build in the end.");
            return false;
        }

        boolean result = false;
        Faction factionAt = Apollo.getInstance().getFactionDao().getFactionAt(location).get();
        if (!(factionAt instanceof ClaimableFaction)) {
            result = true;
        } else if (factionAt instanceof Raidable && ((Raidable) factionAt).isRaidable()) {
            result = true;
        }

        if (player != null && factionAt instanceof PlayerFaction) {
            if (Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).get() == factionAt) {
                result = true;
            }
        }

        if (result) {
            // Show this message last as the other messages look cleaner.
            if (!isInteraction && factionAt instanceof WarzoneFaction) {
                if (denyMessage != null && player != null) {
                    player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You cannot build in the territory of " + factionAt.getDisplayName(player) + ChatColor.GRAY + ".");
                }

                return false;
            }
        } else if (denyMessage != null && player != null) {
            player.sendMessage(String.format(denyMessage, factionAt.getDisplayName(player)));
        }

        return result;
    }

    /**
     * Checks if a {@link Location} is eligible to build into another {@link Location}.
     *
     * @param from the from {@link Location} to test
     * @param to   the to {@link Location} to test
     * @return true if the to {@link Faction} is the same or is not claimable
     */
    public static boolean canBuildAt(Location from, Location to) {
        Faction toFactionAt = Apollo.getInstance().getFactionDao().getFactionAt(to).get();
        return !(toFactionAt instanceof Raidable && !((Raidable) toFactionAt).isRaidable() && toFactionAt != Apollo.getInstance().getFactionDao().getFactionAt(from).get());
    }
}
