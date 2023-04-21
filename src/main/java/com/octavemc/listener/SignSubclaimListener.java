package com.octavemc.listener;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.struct.Role;
import com.octavemc.faction.type.Faction;
import com.octavemc.faction.type.PlayerFaction;
import com.octavemc.util.NameUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.MaterialData;

import java.util.*;
import java.util.stream.Collectors;

public class SignSubclaimListener implements Listener {

    private enum SubclaimType {

        LEADER(ImmutableList.of("[LEADER]", Role.LEADER.getAstrix()),
                ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Leader", "Leader"),

        CAPTAIN(ImmutableList.of("[CAPTAIN]", "[OFFICER]", Role.CAPTAIN.getAstrix()),
                ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Captain", "Captain"),

        MEMBER(ImmutableList.of("[PRIVATE]", "[PERSONAL]", "[SUBCLAIM]", "[MEMBER]"),
                ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Subclaim", "Member");

        private final List<String> aliases;
        private final String outputText;
        private final String displayName;

        SubclaimType(List<String> aliases, String outputText, String displayName) {
            this.aliases = aliases;
            this.outputText = outputText;
            this.displayName = displayName;
        }

        public boolean isEnabled() {
            switch (this) {
                case LEADER:
                    return Configuration.SUBCLAIM_SIGN_LEADER;
                case CAPTAIN:
                    return Configuration.SUBCLAIM_SIGN_CAPTAIN;
                case MEMBER:
                    return Configuration.SUBCLAIM_SIGN_PRIVATE;
                default:
                    return false;
            }
        }
    }

    private static final int MAX_SIGN_LINE_CHARS = 16;
    private static final BlockFace[] SIGN_FACES = new BlockFace[]{
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.UP
    };

    private final Apollo plugin;

    public SignSubclaimListener(Apollo plugin) {
        this.plugin = plugin;
    }

    private SubclaimType getSubclaimType(String value, boolean creating) {
        if (creating) {
            value = value.toUpperCase();
        }

        for (SubclaimType type : SubclaimType.values()) {
            if (creating) {
                if (type.aliases.contains(value)) {
                    return type;
                }
            } else {
                if (type.outputText.equals(value)) {
                    return type;
                }
            }
        }

        return null;
    }

    private boolean isSubclaimable(Block block) {
        return block.getType() == Material.FENCE_GATE || block.getType() == Material.TRAP_DOOR || block.getState() instanceof InventoryHolder;
    }

    private SubclaimType getSubclaimType(Sign sign, boolean creating) {
        SubclaimType subclaimType = getSubclaimType(sign.getLine(0), creating);
        return subclaimType != null && subclaimType.isEnabled() ? subclaimType : null;
    }

    private SubclaimType getSubclaimType(Block block, boolean creating) {
        if (isSubclaimable(block)) {
            Collection<Sign> attachedSigns = getAttachedSigns(block);
            for (Sign attachedSign : attachedSigns) {
                SubclaimType subclaimType = getSubclaimType(attachedSign, creating);
                if (subclaimType != null) {
                    return subclaimType;
                }
            }
        }

        return null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        Block block = event.getBlock();
        MaterialData materialData = block.getState().getData();
        if (materialData instanceof org.bukkit.material.Sign) {
            org.bukkit.material.Sign sign = (org.bukkit.material.Sign) materialData;
            Block attachedBlock = block.getRelative(sign.getAttachedFace());
            if (isSubclaimable(attachedBlock)) {
                Player player = event.getPlayer();
                Optional<PlayerFaction> playerFaction = Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId());
                if (playerFaction.isPresent()) {
                    return; // only allow officers to create Subclaims
                }

                Faction factionAt = Apollo.getInstance().getFactionDao().getFactionAt(block.getLocation()).get();
                if (playerFaction.get() == factionAt) {
                    SubclaimType subclaimType = getSubclaimType(attachedBlock, false);
                    if (subclaimType != null) {
                        player.sendMessage(ChatColor.RED + "There is already a " + subclaimType.displayName + " subclaim sign on this " + NameUtils.getPrettyName(attachedBlock.getType().name()) + '.');
                        return;
                    }

                    String[] lines = event.getLines();
                    subclaimType = getSubclaimType(lines[0], true);
                    if (subclaimType == null || !subclaimType.isEnabled()) {
                        return;
                    }

                    List<String> memberList = null;
                    if (subclaimType == SubclaimType.MEMBER) {
                        memberList = new ArrayList<>(3);
                        for (int i = 1; i < lines.length; i++) {
                            String line = lines[i];
                            if (StringUtils.isNotBlank(line)) {
                                memberList.add(line);
                            }
                        }

                        if (memberList.isEmpty()) {
                            player.sendMessage(ChatColor.RED + "Subclaim signs need to have at least 1 player name inserted.");
                            return;
                        }
                    } else if (subclaimType == SubclaimType.CAPTAIN) {
                        if (playerFaction.get().getMember(player).getRole() == Role.MEMBER) {
                            player.sendMessage(ChatColor.RED + "Only faction officers can create captain subclaimed objects.");
                            return;
                        }

                        // Clear the other lines.
                        event.setLine(1, null);
                        event.setLine(2, null);
                        event.setLine(3, null);
                    } else if (subclaimType == SubclaimType.LEADER) {
                        if (playerFaction.get().getMember(player).getRole() != Role.LEADER) {
                            player.sendMessage(ChatColor.RED + "Only faction leaders can create leader subclaimed objects.");
                            return;
                        }

                        // Clear the other lines.
                        event.setLine(1, null);
                        event.setLine(2, null);
                        event.setLine(3, null);
                    }

                    // Finalise the subclaim.
                    event.setLine(0, subclaimType.outputText);
                    StringBuilder builder = new StringBuilder(Configuration.RELATION_COLOUR_TEAMMATE + player.getName() +
                            ChatColor.YELLOW + " has created a subclaim on block type " + ChatColor.AQUA + NameUtils.getPrettyName(attachedBlock.getType().name()) +
                            ChatColor.YELLOW + " at " + ChatColor.WHITE + '(' + attachedBlock.getX() + ", " + attachedBlock.getZ() + ')' + ChatColor.YELLOW + " for ");

                    if (subclaimType == SubclaimType.LEADER) {
                        builder.append("leaders");
                    } else if (subclaimType == SubclaimType.CAPTAIN) {
                        builder.append("captains");
                    } else if (memberList != null) { // Should never be null, but best safe; SubclaimType.PRIVATE
                        builder.append("members ").append(ChatColor.RED).append('[');
                        builder.append(memberList.stream().filter(string -> playerFaction.get().getMember(string) != null).collect(Collectors.joining(", "))).append("]");
                    }

                    playerFaction.get().broadcast(builder.toString());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getEotwHandler().isEndOfTheWorld()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {
            return;
        }

        Block block = event.getBlock();
        BlockState state = block.getState();

        Block subclaimObjectBlock = null;
        if (state instanceof Sign) {
            Sign sign = (Sign) state;
            MaterialData signData = sign.getData();
            if (signData instanceof org.bukkit.material.Sign) {
                org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign) signData;
                subclaimObjectBlock = block.getRelative(materialSign.getAttachedFace());
            }
        } else {
            subclaimObjectBlock = block;
        }

        if (subclaimObjectBlock != null && !checkSubclaimIntegrity(player, subclaimObjectBlock)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break this subclaimed " + NameUtils.getPrettyName(subclaimObjectBlock.getType().name()) + '.');
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (plugin.getEotwHandler().isEndOfTheWorld() || !Configuration.SUBCLAIM_HOPPER_CHECK) {
            return;
        }

        // Have to do this hackery since Bukkit doesn't
        // provide an API for us to do this
        InventoryHolder holder = event.getSource().getHolder();
        Collection<Block> sourceBlocks;
        if (holder instanceof Chest) {
            sourceBlocks = Collections.singletonList(((Chest) holder).getBlock());
        } else if (holder instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) holder;
            sourceBlocks = Lists.newArrayList(((Chest) doubleChest.getLeftSide()).getBlock(), ((Chest) doubleChest.getRightSide()).getBlock());
        } else {
            return;
        }

        for (Block block : sourceBlocks) {
            if (getSubclaimType(block, false) != null) {
                event.setCancelled(true);
                break;
            }
        }
    }

    private String getShortenedName(String originalName) {
        if (originalName.length() >= MAX_SIGN_LINE_CHARS) {
            originalName = originalName.substring(0, MAX_SIGN_LINE_CHARS);
        }

        return originalName;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {
                return;
            }

            if (plugin.getEotwHandler().isEndOfTheWorld()) {
                return;
            }

            Block block = event.getClickedBlock();
            if (!checkSubclaimIntegrity(player, block)) {
                event.setUseInteractedBlock(Event.Result.DENY);
                player.sendMessage(ChatColor.RED + "You do not have access to this subclaimed " + NameUtils.getPrettyName(block.getType().name()) + '.');
            }
        }
    }

    /**
     * Checks subclaim integrity of a {@link Block} for a {@link Player}.
     *
     * @param player         the {@link Player} to check
     * @param subclaimObject the {@link Block} to check
     * @return true if allowed to open
     */
    private boolean checkSubclaimIntegrity(Player player, Block subclaimObject) {
        if (!isSubclaimable(subclaimObject)) {
            return true; // Not even subclaimed.
        }

        Optional<PlayerFaction> playerFaction = Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId());
        if (playerFaction.isPresent() || playerFaction.get().isRaidable()) {
            return true; // If the faction is raidable, just allow it.
        }

        Role role = playerFaction.get().getMember(player).getRole();
        if (role == Role.LEADER) {
            return true; // Let leaders open regardless.
        }

        if (!playerFaction.get().equals(Apollo.getInstance().getFactionDao().getFactionAt(subclaimObject.getLocation()).get())) {
            return true; // Let enemies be able to open
        }

        Collection<Sign> attachedSigns = getAttachedSigns(subclaimObject);
        if (attachedSigns.isEmpty()) {
            return true;
        }

        boolean flag = true;
        String search = null; // lazy-loaded
        for (Sign attachedSign : attachedSigns) {
            SubclaimType subclaimType = getSubclaimType(attachedSign, false);
            if (subclaimType == null) {
                continue;
            }

            // No need to conditional check leaders as they can open anything.
            if (subclaimType == SubclaimType.CAPTAIN) {
                if (role == Role.MEMBER) {
                    flag = false;
                    continue;
                }

                return true;
            } else if (subclaimType == SubclaimType.MEMBER) {
                if (search == null) {
                    search = getShortenedName(player.getName());
                }

                String[] lines = attachedSign.getLines();
                for (int i = 1; i < lines.length; i++) {
                    if (lines[i].contains(search)) {
                        return true;
                    }
                }
            }
        }

        return flag;
    }

    /**
     * Gets the attached {@link Sign}s on a {@link Block}.
     *
     * @param block the {@link Block} to get for
     * @return collection of attached {@link Sign}s
     */
    public Collection<Sign> getAttachedSigns(Block block) {
        LinkedHashSet<Sign> results = new LinkedHashSet<>();
        getSignsAround(block, results);

        BlockState state = block.getState();
        if (state instanceof Chest) {
            Inventory chestInventory = ((Chest) state).getInventory();
            if (chestInventory instanceof DoubleChestInventory) {
                DoubleChest doubleChest = ((DoubleChestInventory) chestInventory).getHolder();
                Block left = ((Chest) doubleChest.getLeftSide()).getBlock();
                Block right = ((Chest) doubleChest.getRightSide()).getBlock();
                getSignsAround(left.equals(block) ? right : left, results);
            }
        }

        return results;
    }

    /**
     * Populates a given set with the {@link Sign}s that are attached
     * to a given {@link Block}.
     *
     * @param block   the {@link Block} to get around
     * @param results the input to add to
     * @return the updated set of {@link Sign}s
     */
    private Set<Sign> getSignsAround(Block block, LinkedHashSet<Sign> results) {
        for (BlockFace face : SIGN_FACES) {
            Block relative = block.getRelative(face);
            BlockState relativeState = relative.getState();
            if (relativeState instanceof Sign) {
                org.bukkit.material.Sign materialSign = (org.bukkit.material.Sign) relativeState.getData();
                if (relative.getRelative(materialSign.getAttachedFace()).equals(block)) {
                    results.add((Sign) relative.getState());
                }
            }
        }

        return results;
    }
}
