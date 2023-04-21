package com.octavemc.faction;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.eventgame.faction.EventFaction;
import com.octavemc.faction.claim.ClaimHandler;
import com.octavemc.faction.event.FactionRelationCreateEvent;
import com.octavemc.faction.event.FactionRelationRemoveEvent;
import com.octavemc.faction.struct.ChatChannel;
import com.octavemc.faction.struct.Relation;
import com.octavemc.faction.struct.Role;
import com.octavemc.faction.type.Faction;
import com.octavemc.faction.type.PlayerFaction;
import com.octavemc.timer.type.StuckTimer;
import com.octavemc.user.User;
import com.octavemc.util.BukkitUtils;
import com.octavemc.util.DurationFormatter;
import com.octavemc.visualise.VisualType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.octavemc.util.SpigotUtils.toBungee;

@CommandAlias("factions|faction|f")
public class FactionsCommand extends BaseCommand {

    public FactionsCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @HelpCommand
    public static void onHelpCommand(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("create")
    @Description("Create a faction.")
    public static void onCreateCommand(Player player, String name) {
        if (Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).isPresent()) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are already in a faction.");
        } else if (Configuration.FACTION_DISSALLOWED_NAMES.contains(name)) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The name '" + ChatColor.AQUA + name + "' is not a permitted faction name.");
        } else if (name.length() < Configuration.FACTION_NAME_MINIMUM_CHARACTERS) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Faction names must have at least " + ChatColor.AQUA + Configuration.FACTION_NAME_MINIMUM_CHARACTERS + " characters.");
        } else if (name.length() > Configuration.FACTION_NAME_MAXIMUM_CHARACTERS) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Faction names cannot be longer than " + ChatColor.AQUA + Configuration.FACTION_NAME_MAXIMUM_CHARACTERS + " characters.");
        } else if (!StringUtils.isAlphanumeric(name)) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Faction names may only be alphanumeric.");
        } else if (Apollo.getInstance().getFactionDao().getByName(name).isPresent()) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name already exists!");
        } else {
            Apollo.getInstance().getFactionDao().create(new PlayerFaction(name), player);
        }
    }

    @Subcommand("disband")
    @Description("Disband your faction.")
    public static void onDisbandCommand(Player player) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.isRaidable() && !Apollo.getInstance().getEotwHandler().isEndOfTheWorld()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You cannot disband your faction while you are raidable!");
            } else if (faction.getMember(player).getRole() != Role.LEADER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a leader to disband the faction.");
            } else if (Apollo.getInstance().getFactionDao().remove(faction, player)) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You have disbanded your faction!");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("invites")
    @Description("")
    public static void onInvitesCommand(Player player) {
        //TODO: Transfer over recievedInviteNames to uuids.
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            player.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
            if (faction.getInvitedPlayerNames().isEmpty()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "No invitations are pending response...");
            } else {
                faction.getInvitedPlayerNames().forEach(name -> player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + name));
            }
            player.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        }, () -> {
            List<String> factions = Apollo.getInstance().getFactionDao().getCache().values().stream().filter(target -> target instanceof PlayerFaction).filter(target -> ((PlayerFaction) target).getInvitedPlayerNames().contains(player.getName())).map(Faction::getName).collect(Collectors.toList());
            player.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
            if (factions.isEmpty()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "No invitations are pending response...");
            } else {
                factions.forEach(name -> player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + name));
            }
            player.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        });
    }

    @Subcommand("invite")
    //TODO: Command Completion All Players, not in faction?
    public static void onInviteCommand(Player player, OfflinePlayer target) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (!target.isOnline() || !target.hasPlayedBefore()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "That player is not online.");
            } else if (faction.getMember(player).getRole() == Role.MEMBER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must a faction officer to invite members.");
            } else if (faction.isRaidable() && !Apollo.getInstance().getEotwHandler().isEndOfTheWorld()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not invite players to your faction while you are raidable!");
            } else if (!faction.getInvitedPlayerNames().add(target.getName())) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.AQUA + target.getName() + ChatColor.GRAY + " has already been invited to the faction.");
            } else {
                net.md_5.bungee.api.ChatColor enemyRelationColor = toBungee(Relation.ENEMY.toChatColour());
                ComponentBuilder builder = new ComponentBuilder(toBungee(ChatColor.DARK_GRAY) + "" + toBungee(ChatColor.BOLD) + "(" + toBungee(ChatColor.AQUA) + "" + toBungee(ChatColor.BOLD) + "!" + toBungee(ChatColor.DARK_GRAY) + "" + toBungee(ChatColor.BOLD) + ") ")
                        .append("You have been invited to join ").color(net.md_5.bungee.api.ChatColor.GRAY)
                        .append(faction.getName()).color(enemyRelationColor)
                        .append(" by ").color(net.md_5.bungee.api.ChatColor.GRAY)
                        .append(player.getName()).color(enemyRelationColor)
                        .append(". ").color(net.md_5.bungee.api.ChatColor.GRAY)
                        .append("Click here to join the faction")
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/factions accept " + faction.getName()))
                        .color(net.md_5.bungee.api.ChatColor.AQUA)
                        .append(".").color(net.md_5.bungee.api.ChatColor.GRAY);
                target.getPlayer().spigot().sendMessage(builder.create());
                faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + Relation.MEMBER.toChatColour() + player.getName() + ChatColor.GRAY + " has invited " + Relation.ENEMY.toChatColour() + target.getName() + ChatColor.GRAY + " to the faction.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("accept|join")
    //TODO: Command Completion Factions Invited To
    public static void onAcceptCommand(Player player, String name) {
        Apollo.getInstance().getFactionDao().getContaining(name).ifPresentOrElse(faction -> {
            if (Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).isPresent()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are already in a faction.");
            } else if (!(faction instanceof PlayerFaction)) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name was not found.");
            } else if (((PlayerFaction) faction).getMembers().size() >= Configuration.FACTION_MAXIMUM_MEMBERS) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The faction " + faction.getDisplayName(player) + " has reached it member limit of " + ChatColor.AQUA + Configuration.FACTION_MAXIMUM_MEMBERS + " members " +  ChatColor.GRAY + ".");
            } else if (!((PlayerFaction) faction).getInvitedPlayerNames().contains(player.getName())) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The faction " + faction.getDisplayName(player) + " has not sent you an invite.");
            } else if (((PlayerFaction) faction).addMember(player, player, player.getUniqueId(), new FactionMember(player.getUniqueId(), ChatChannel.PUBLIC, Role.MEMBER))) {
                ((PlayerFaction) faction).broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + Relation.MEMBER.toChatColour() + player.getName() + ChatColor.GRAY + " is now a member of the faction.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name was not found."));
    }

    @Subcommand("uninvite")
    @Syntax("<all|player>")
    //TODO: Command Completion Faction Invites.
    public static void onInviteRevokeCommand(Player player, String name) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.getMember(player).getRole() == Role.MEMBER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a faction officer to un-invite players.");
            } else if (name.equalsIgnoreCase("all")) {
                faction.getInvitedPlayerNames().clear();
                player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have cleared all invitations.");
            } else if (!faction.getInvitedPlayerNames().remove(name)) {
                player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "A invitee with that name does not exist.");
            } else {
                faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + faction.getMember(player).getRole().getAstrix() + player.getName() + " has revoked " + Configuration.RELATION_COLOUR_ENEMY + name + ChatColor.YELLOW + " invitation to the faction.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name was not found."));
    }

    @Subcommand("kick")
    @Syntax("<player>")
    //TODO: Command Completion Faction Members, who are not leader and are not the player and other captains.
    //TODO: Find a better way to implement this.
    public static void onKickCommand(Player player, String name) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.getMember(player).getRole() == Role.MEMBER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a faction officer to kick members.");
            } else if (faction.isRaidable() && !Apollo.getInstance().getEotwHandler().isEndOfTheWorld()){
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not kick players while you are raidable.");
            } else if (faction.getMember(name) == null) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction member with that name does not exist.");
            } else if (faction.getMember(name).getRole() == Role.LEADER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not kick the leader of the faction.");
            } else if (faction.getMember(player).getRole() == Role.CAPTAIN && faction.getMember(name).getRole() == Role.CAPTAIN) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a faction leader to kick captains.");
            } else {
                faction.removeMember(player, Apollo.getInstance().getServer().getPlayer(name), faction.getMember(name).getUniqueID(), true, true);
                if (Apollo.getInstance().getServer().getPlayer(name) != null) {
                    Apollo.getInstance().getServer().getPlayer(name).sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You were kicked from the faction by " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + ".");
                }
                faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + Configuration.RELATION_COLOUR_TEAMMATE + faction.getMember(player).getRole().getAstrix() + player.getName() + ChatColor.AQUA + " has kicked " +
                        Configuration.RELATION_COLOUR_TEAMMATE + name + ChatColor.GRAY + " from the faction.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("leave")
    @Description("Leave your current faction.")
    public static void onLeaveCommand(Player player) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.getMember(player).getRole() == Role.LEADER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You cannot leave the faction as a leader, disband your faction using " + ChatColor.AQUA + "/factions disband" + ChatColor.GRAY + " or promote another member to leader using " + ChatColor.AQUA + "/factions leader" + ChatColor.GRAY + ".");
            } else if (faction.removeMember(player, player, player.getUniqueId(), false, false)) {
                player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have successfully left the faction.");
                faction.broadcast(Configuration.DANGER_MESSAGE_PREFIX + Relation.ENEMY.toChatColour() + player.getName() + ChatColor.GRAY + " has left the faction.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("leader")
    @Syntax("<player>")
    @Description("")
    //TODO: Command Completion Faction Members, who are not leader
    public static void onLeaderCommand(Player player, String name) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.getMember(player).getRole() != Role.LEADER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be the be a faction leader to transfer leadership of the faction.");
            } else if (faction.getMember(name) == null) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction member with that name was not found.");
            } else if (faction.getLeader().getName().equalsIgnoreCase(name)) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are already the faction leader.");
            } else {
                faction.getMember(name).setRole(Role.LEADER);
                faction.getMember(player).setRole(Role.CAPTAIN);
                faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + Configuration.RELATION_COLOUR_TEAMMATE + Role.CAPTAIN.getAstrix() + player.getName() + ChatColor.GRAY +
                        " has transferred the faction leadership to " + Configuration.RELATION_COLOUR_TEAMMATE + Role.LEADER.getAstrix() + name + ChatColor.GRAY + '.');
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("promote")
    @Syntax("<player>")
    @Description("Promotes a player to a captain.")
    //TODO: Command completion of all players that are promoteable?
    public static void onPromoteCommand(Player player, String name) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.getMember(player).getRole() != Role.LEADER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a faction leader to promote members to captain.");
            } else if (faction.getMember(name) == null) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction member with that name was not found.");
            } else if (faction.getMember(name).getRole() != Role.MEMBER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can only promote members who are not officers.");
            } else {
                faction.getMember(name).setRole(Role.CAPTAIN);
                faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + Relation.MEMBER.toChatColour() + faction.getMember(name).getRole().getAstrix() + name + ChatColor.GRAY + " has been promoted to a faction captain.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("demote")
    @Syntax("<player>")
    @Description("Demotes a player to a member.")
    //TODO: Command completion of all players that are demoteable?
    public static void onDemoteCommand(Player player, String name) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.getMember(player).getRole() != Role.LEADER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be an faction officer to demote members.");
            } else if (faction.getMember(name) == null) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction member with that name does not exist.");
            } else if (faction.getMember(name).getRole() != Role.CAPTAIN) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can only demote members who are captains.");
            } else {
                faction.getMember(name).setRole(Role.MEMBER);
                faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + Relation.MEMBER.toChatColour() + faction.getMember(name).getRole().getAstrix() + name + ChatColor.GRAY + " has been demoted to a member.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("deposit")
    @Syntax("<amount>")
    @Description("Deposits money to the faction balance.")
    //TODO: Command completion of whole player balance.
    public static void onDepositCommand(Player player, int amount) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (amount <= 0) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The amount must be positive.");
            } else if (amount > Apollo.getInstance().getUserDao().get(player.getUniqueId()).get().getBalance()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You do not have enough money, you only have " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + Apollo.getInstance().getUserDao().get(player.getUniqueId()).get().getBalance() + ChatColor.GRAY + '.');
            } else {
                Apollo.getInstance().getUserDao().get(player.getUniqueId()).ifPresent(user -> user.setBalance(user.getBalance() - amount));
                faction.setBalance(faction.getBalance() + amount);
                faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + Relation.MEMBER.toChatColour() + faction.getMember(player).getRole().getAstrix() + player.getName() + ChatColor.GRAY + " has deposited " +
                        ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + amount + ChatColor.GRAY + " into the faction balance.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("withdraw")
    @Syntax("<amount>")
    @Description("Withdraws money from the faction balance.")
    //TODO: Command completion of the whole faction balance.
    public static void onWithdrawCommand(Player player, int amount) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.getMember(player).getRole() == Role.MEMBER) {
                player.sendMessage();
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a faction officer to withdraw money.");
            } else if (amount <= 0) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The amount must be positive.");
            } else if (amount > faction.getBalance()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction does not have enough money, your faction only has " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + faction.getBalance() + ChatColor.GRAY + '.');
            } else {
                Apollo.getInstance().getUserDao().get(player.getUniqueId()).ifPresent(user -> user.setBalance(user.getBalance() + amount));
                faction.setBalance(faction.getBalance() - amount);
                faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + Relation.MEMBER.toChatColour() + faction.getMember(player).getRole().getAstrix() + player.getName() + ChatColor.GRAY + " has withdrawn " +
                        ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + amount + ChatColor.GRAY + " from the faction balance.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("announcement")
    @Syntax("<clear|announcement>")
    @Description("Set your faction announcement.")
    //TODO: Test this command.
    public static void onAnnouncementCommand(Player player, String announcement) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.getMember(player).getRole() == Role.MEMBER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a faction officer to change the faction announcement.");
            } else if (announcement.equalsIgnoreCase("clear")) {
                faction.setAnnouncement(null);
                faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "The faction announcement has been cleared by " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + '.');
            } else {
                faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "The faction announcement has been updated from "
                        + ChatColor.AQUA + (faction.getAnnouncement() != null ? faction.getAnnouncement() : "nothing")
                        + ChatColor.GRAY + " to " + ChatColor.AQUA + announcement + ChatColor.GRAY + '.');
                faction.setAnnouncement(announcement);
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    //TODO: Move these to Configuration.java
    private static final long FACTION_RENAME_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(15L);

    @Subcommand("rename")
    @Syntax("<identifier>")
    @Description("Change the name of your faction.")
    public static void onRenameCommand(Player player, String identifier) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (Configuration.FACTION_DISSALLOWED_NAMES.contains(identifier)) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The provided name is a disallowed faction name.");
            } else if (identifier.length() < Configuration.FACTION_NAME_MINIMUM_CHARACTERS) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction name must have at least " + Configuration.FACTION_NAME_MINIMUM_CHARACTERS + " characters.");
            } else if (identifier.length() > Configuration.FACTION_NAME_MAXIMUM_CHARACTERS) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction name must have at most " + Configuration.FACTION_NAME_MAXIMUM_CHARACTERS + " characters.");
            } else if (!StringUtils.isAlphanumeric(identifier)) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction name must be alphanumeric.");
            } else if (Apollo.getInstance().getFactionDao().getByName(identifier).isPresent()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with the name " + ChatColor.AQUA + identifier + ChatColor.GRAY + " already exists.");
            } else {
                //TODO: Check if the player has the bypass permission.
                long difference = faction.lastRenameMillis - System.currentTimeMillis() + FACTION_RENAME_DELAY_MILLIS;
                if (difference > 0L) player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You need to wait another " + DurationFormatUtils.formatDurationWords(difference, true, true) + " to rename your faction.");
                else faction.setName(identifier, player);
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("set home")
    @Description("Sets the faction home location.")
    public static void onSetHomeCommand(Player player) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.getMember(player).getRole() == Role.MEMBER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a faction officer to set the faction home.");
            } else if (!Apollo.getInstance().getFactionDao().getClaimAt(player.getLocation()).get().getFaction().equals(faction)) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY +" You can only set the faction home in your territory.");
            } else if (Configuration.MAX_HEIGHT_FACTION_HOME != -1 && player.getLocation().getBlockY() > Configuration.MAX_HEIGHT_FACTION_HOME) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not set the faction home above the height limit of " + ChatColor.AQUA + Configuration.MAX_HEIGHT_FACTION_HOME + ChatColor.GRAY + '.');
            } else {
                faction.setHome(player.getLocation());
                faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "The faction home has been updated by " + Configuration.RELATION_COLOUR_TEAMMATE + faction.getMember(player).getRole().getAstrix() + player.getName());
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("home")
    @Description("Teleport to the faction home.")
    public static void onHomeCommand(Player player) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (Apollo.getInstance().getTimerManager().getEnderpearlTimer().getRemaining(player) > 0L) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not teleport home while your " + ChatColor.AQUA + Apollo.getInstance().getTimerManager().getEnderpearlTimer().getName() + ChatColor.GRAY + " cooldown is active.");
            } else if (Apollo.getInstance().getTimerManager().getCombatTimer().getRemaining(player) > 0L) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not teleport home while you are " + ChatColor.AQUA + "combat tagged" + ChatColor.GRAY + '.');
            } else if (faction.getHome() == null) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction does not have a home set.");
            } else if (Apollo.getInstance().getFactionDao().getFactionAt(player.getLocation()).get() instanceof EventFaction) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not teleport home while in the territory of a event.");
            } else if (Apollo.getInstance().getFactionDao().getFactionAt(player.getLocation()).get() instanceof PlayerFaction && !Configuration.ALLOW_TELEPORTING_IN_ENEMY_TERRITORY && !Apollo.getInstance().getFactionDao().getFactionAt(player.getLocation()).get().equals(faction)) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not teleport home in enemy territory. Use the command " + ChatColor.AQUA + "/factions stuck" + ChatColor.GRAY + " if you are trapped.");
            } else {
                //TODO: Check if teleportation is disabled in that world.
                switch (player.getWorld().getEnvironment()) {
                    case THE_END ->
                        Apollo.getInstance().getTimerManager().getTeleportTimer().teleport(player, faction.getHome(), Configuration.FACTION_HOME_TELEPORT_DELAY_END_MILLIS,
                                Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Teleporting to your faction home in " + ChatColor.AQUA + DurationFormatter.getRemaining(Configuration.FACTION_HOME_TELEPORT_DELAY_END_MILLIS, true, false) + ChatColor.GRAY + ", taking damage or moving will cancel this.",
                                PlayerTeleportEvent.TeleportCause.COMMAND);
                    case NETHER ->
                        Apollo.getInstance().getTimerManager().getTeleportTimer().teleport(player, faction.getHome(), Configuration.FACTION_HOME_TELEPORT_DELAY_NETHER_MILLIS,
                                Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Teleporting to your faction home in " + ChatColor.AQUA + DurationFormatter.getRemaining(Configuration.FACTION_HOME_TELEPORT_DELAY_NETHER_MILLIS, true, false) + ChatColor.GRAY + ", taking damage or moving will cancel this.",
                                PlayerTeleportEvent.TeleportCause.COMMAND);
                    case NORMAL ->
                        Apollo.getInstance().getTimerManager().getTeleportTimer().teleport(player, faction.getHome(), Configuration.FACTION_HOME_TELEPORT_DELAY_OVERWORLD_MILLIS,
                                Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Teleporting to your faction home in " + ChatColor.AQUA + DurationFormatter.getRemaining(Configuration.FACTION_HOME_TELEPORT_DELAY_OVERWORLD_MILLIS, true, false) + ChatColor.GRAY + ", taking damage or moving will cancel this.",
                                PlayerTeleportEvent.TeleportCause.COMMAND);
                }
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("stuck")
    @Description("")
    public static void onStuckCommand(Player player) {
        StuckTimer timer = Apollo.getInstance().getTimerManager().getStuckTimer();

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can only execute this command from the overworld.");
        } else if (!timer.setCooldown(player, player.getUniqueId())) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You have already activated your " + ChatColor.AQUA + timer.getName() + ChatColor.GRAY + "timer.");
        } else {
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + "Your " + ChatColor.AQUA + timer.getName()
                    + ChatColor.GRAY + " timer has started, you will be teleported to the nearest safe area in "
                    + ChatColor.AQUA + DurationFormatter.getRemaining(timer.getRemaining(player), true, false)
                    + ChatColor.GRAY + ". Moving " + ChatColor.AQUA + StuckTimer.MAX_MOVE_DISTANCE + ChatColor.GRAY + " blocks will cancel this.");
        }
    }

    @Subcommand("claim")
    @Description("Claim land in the Wilderness")
    public static void onClaimCommand(Player player) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.isRaidable()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not claim land while your faction is raidable.");
            } else if (player.getInventory().contains(ClaimHandler.CLAIM_WAND)
                    || player.getInventory().contains(ClaimHandler.SUBCLAIM_WAND)
                    || player.getInventory().contains(ClaimHandler.EVENT_CLAIM_WAND)) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You already have a claiming wand in your inventory.");
            } else if (!player.getInventory().addItem(ClaimHandler.CLAIM_WAND).isEmpty()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your inventory is full, empty one slot and rerun this command.");
            } else {
                player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "A claiming wand has been added to your inventory, read the item lore to instructions on how to claim.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("unclaim")
    @Description("")
    public static void onUnclaimCommand(Player player) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.getMember(player).getRole() != Role.LEADER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a faction leader to unclaim land.");
            } else if (faction.getClaims().isEmpty()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction does not have any claims.");
            } else {
                Apollo.getInstance().getFactionDao().getClaimAt(player.getLocation()).filter(claim -> faction.getClaims().contains(claim)).ifPresentOrElse(claim -> {
                    if (faction.removeClaim(claim, player)) {
                        faction.broadcast(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.AQUA + faction.getMember(player).getRole().getAstrix() + player.getName() + ChatColor.GRAY + " has unclaimed land.");
                    }
                }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction does not have a claim here."));
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("unclaim all")
    @Description("")
    public static void onUnclaimAllCommand(Player player) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            if (faction.getMember(player).getRole() != Role.LEADER) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a faction leader to unclaim land.");
            } else if (faction.getClaims().isEmpty()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction does not have any claims.");
            } else {
                if (!faction.removeClaims(faction.getClaims(), player)) {
                    player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "An error occurred when removing claims, please contact an administrator.");
                } else {
                    faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + faction.getMember(player).getRole().getAstrix() +
                            player.getName() + ChatColor.GRAY + " has unclaimed all land.");
                }
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Subcommand("ally")
    @Syntax("<faction>")
    @Description("")
    //TODO: Command completion of all factions?
    @CommandCompletion("@players")
    public static void onAllyCommand(Player player, String name) {
        if (Configuration.FACTION_MAXIMUM_ALLIES <= 0) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Allying is disabled for this season.");
        } else {
            Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
                if (faction.getMember(player.getUniqueId()).getRole() == Role.MEMBER) {
                    player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a faction officer to ally.");
                } else {
                    Apollo.getInstance().getFactionDao().getContaining(name).ifPresentOrElse(target -> {
                        if (faction.equals(target)) {
                            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not send alliance requests to your own faction.");
                        } else if (faction.getAllied().size() >= Configuration.FACTION_MAXIMUM_ALLIES) {
                            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction already has the maximum amount of allies.");
                        } else if (((PlayerFaction) target).getAllied().size() >= Configuration.FACTION_MAXIMUM_ALLIES) {
                            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The faction " + ChatColor.AQUA + target.getDisplayName(faction) + ChatColor.GRAY + " already has the maximum amount of allies.");
                        } else if (faction.getAllied().contains(target.getIdentifier())) {
                            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction is already allied with " + target.getDisplayName(faction) + ChatColor.GRAY + '.');
                        } else if (((PlayerFaction) target).getRequestedRelations().remove(faction.getIdentifier()) != null) {
                            var event = new FactionRelationCreateEvent(faction, (PlayerFaction) target, Relation.ALLY);
                            Apollo.getInstance().getServer().getPluginManager().callEvent(event);

                            ((PlayerFaction) target).getRelations().putIfAbsent(faction.getIdentifier(), Relation.ALLY);
                            ((PlayerFaction) target).broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction is now allied with " + faction.getDisplayName(target) + ChatColor.GRAY + '.');

                            faction.getRelations().putIfAbsent(target.getIdentifier(), Relation.ALLY);
                            faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction is now allied with " + target.getDisplayName(faction) + ChatColor.GRAY + '.');

                        } else {
                            if (faction.getRequestedRelations().putIfAbsent(target.getIdentifier(), Relation.ALLY) != null) {
                                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction has already sent a alliance request to " + target.getDisplayName(faction) + ChatColor.GRAY + '.');
                            }
                            faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + target.getDisplayName(faction) + ChatColor.GRAY + " was sent a alliance request.");
                            ((PlayerFaction) target).broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + faction.getDisplayName(target) + ChatColor.GRAY + " has requested to be in an alliance, use " + ChatColor.AQUA + "/factions ally" + ChatColor.GRAY + " to accept.");
                        }
                    }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name or player was not found."));

                }
            }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
        }
    }

    @Subcommand("unally")
    @Syntax("<faction>")
    @Description("")
    //TODO: Tab completion of allies?
    public static void onUnallyCommand(Player player, String name) {
        if (Configuration.FACTION_MAXIMUM_ALLIES <= 0) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Allying is disabled for this season.");
        } else {
            Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
                if (faction.getMember(player).getRole() == Role.MEMBER) {
                    player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be a faction officer to unally.");
                    return;
                }

                Apollo.getInstance().getFactionDao().getContaining(name).ifPresentOrElse(target -> {
                    if (faction.getRelations().remove(target.getIdentifier()) == null || ((PlayerFaction) target).getRelations().remove(faction.getIdentifier()) == null) {
                        player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction is not allied with " + target.getDisplayName(faction) + ChatColor.GRAY + '.');
                        return;
                    }

                    var event = new FactionRelationRemoveEvent(faction, (PlayerFaction) target, Relation.ALLY);
                    Apollo.getInstance().getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled())
                        player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Failed to unally " + target.getDisplayName(player) + ChatColor.GRAY + '.');


                    faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction is no longer allied to "+ target.getDisplayName(player) + ChatColor.GRAY + '.');
                    ((PlayerFaction) target).broadcast(Configuration.WARNING_MESSAGE_PREFIX + faction.getDisplayName(target) + ChatColor.GRAY + " has unallied your faction.");
                    }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name or player was not found."));
            }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
        }
    }

    @Subcommand("show")
    @Syntax("<faction|player>")
    @Description("")
    //TODO: Command completion of all factions, or players?
    public static void onShowCommand(Player player, @Optional String name) {
        if (name == null) {
            Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> faction.printDetails(player),
                    () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You currently aren't in a faction, create one using /factions create <name>."));
        } else {
            Apollo.getInstance().getFactionDao().getContaining(name).ifPresentOrElse(faction -> faction.printDetails(player),
                    () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name or player was not found."));
        }
    }

    @Subcommand("top")
    @Description("View a list of top factions by balance.")
    public static void onTop(CommandSender sender) {
         new BukkitRunnable() {
            @Override
            public void run() {
                sender.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
                Apollo.getInstance().getFactionDao().getCache().values().stream()
                        .filter(faction -> faction instanceof PlayerFaction)
                        .sorted(Comparator.comparingInt(faction -> ((PlayerFaction) faction).getBalance()).reversed())
                        .limit(10)
                        .forEachOrdered(faction -> sender.sendMessage(faction.getDisplayName(sender) + ChatColor.GRAY + " - " + ChatColor.WHITE + Configuration.ECONOMY_SYMBOL + ((PlayerFaction) faction).getBalance()));
                sender.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
            }
        }.runTaskAsynchronously(Apollo.getInstance());
    }

    @Subcommand("map")
    public static void onMapCommand(Player player) {
        User user = Apollo.getInstance().getUserDao().get(player.getUniqueId()).get();

        if (user.isShowClaimMap()) {
            user.setShowClaimMap(false);
            Apollo.getInstance().getVisualiseHandler().clearVisualBlocks(player, VisualType.CLAIM_MAP, null);
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Claim pillars are now hidden.");
        } else {
            user.setShowClaimMap(true);
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Claim pillars are now visible.");
            LandMap.updateMap(player, Apollo.getInstance(), VisualType.CLAIM_MAP, true);
        }
    }

    @Subcommand("chat")
    public static void onChatCommand(Player player) {
        Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).ifPresentOrElse(faction -> {
            faction.getMember(player).setChatChannel(faction.getMember(player).getChatChannel().getRotation());
            player.sendMessage(ChatColor.YELLOW + "You are now in " + ChatColor.AQUA + faction.getMember(player).getChatChannel().getDisplayName().toLowerCase() + ChatColor.YELLOW + " chat mode.");
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not in a faction."));
    }

    @Private
    @Subcommand("force join")
    @CommandPermission("apollo.commands.factions.force.join")
    public static void onForceJoinCommand(Player player, String name) {
        Apollo.getInstance().getFactionDao().getContaining(name).filter(faction -> faction instanceof PlayerFaction).ifPresentOrElse(faction -> {
            if (((PlayerFaction) faction).addMember(player, player, player.getUniqueId(), new FactionMember(player.getUniqueId(), ChatChannel.PUBLIC, Role.MEMBER))) {
                ((PlayerFaction) faction).broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + player.getName() + ChatColor.GRAY + " has forcibly joined your faction.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name or player was not found."));
    }

    @Private
    @Subcommand("force kick")
    @CommandPermission("apollo.commands.factions.force.kick")
    public static void onForceKickCommand(CommandSender sender, String name, OfflinePlayer target) {
        Apollo.getInstance().getFactionDao().getContaining(name).filter(faction -> faction instanceof PlayerFaction).ifPresentOrElse(faction -> {
            var member = ((PlayerFaction) faction).getMember(target.getUniqueId());
            if (member == null) {
                sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction member with that name was not found.");
            } else if (member.getRole() == Role.LEADER) {
                sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not forcibly kick leaders, use " + ChatColor.AQUA + "/factions force leader <player>" + ChatColor.GRAY + ".");
            } else if (((PlayerFaction) faction).removeMember(sender, target.getPlayer(), target.getUniqueId(), true, true)) {
                if (Apollo.getInstance().getServer().getPlayer(name) != null) {
                    Apollo.getInstance().getServer().getPlayer(name).sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You have been forcibly kicked from the faction by " + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + ".");
                }
                ((PlayerFaction) faction).broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + member.getName() + ChatColor.GRAY + " has been forcibly kicked from your faction by "  + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + ".");
            }

        }, () -> sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name or player was not found."));
    }

    @Private
    @Subcommand("force leader")
    @CommandPermission("apollo.commands.factions.force.leader")
    public static void onForceLeaderCommand(CommandSender sender, String name, OfflinePlayer target) {
        Apollo.getInstance().getFactionDao().getContaining(name).filter(faction -> faction instanceof PlayerFaction).ifPresentOrElse(faction -> {
            var member = ((PlayerFaction) faction).getMember(target.getUniqueId());
            if (member == null) {
                sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction member with that name was not found.");
            } else if (member.getRole() == Role.LEADER) {
                sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The member " + ChatColor.AQUA + member.getName() + ChatColor.GRAY + " is already the leader of the faction.");
            } else {
                ((PlayerFaction) faction).broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + member.getName() + ChatColor.GRAY + " has been forcibly kicked from your faction by "  + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + ".");
            }
        }, () -> sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name or player was not found."));
    }

    @Private
    @Subcommand("force promote")
    @CommandPermission("apollo.commands.factions.force.promote")
    public static void onForcePromoteCommand(CommandSender sender, String name, OfflinePlayer target) {
        Apollo.getInstance().getFactionDao().getContaining(name).filter(faction -> faction instanceof PlayerFaction).ifPresentOrElse(faction -> {
            var member = ((PlayerFaction) faction).getMember(target.getUniqueId());
            if (member == null) {
                sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction member with that name was not found.");
            } else if (member.getRole() != Role.MEMBER) {
                sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not forcibly promote " + ChatColor.AQUA + member.getName() + ChatColor.GRAY + " because they are already a " + ChatColor.AQUA + member.getRole().getName() + ChatColor.GRAY + ".");
            } else {
                member.setRole(Role.CAPTAIN);
                ((PlayerFaction) faction).broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + member.getName() + ChatColor.GRAY + " has been forcibly promoted to a " + ChatColor.AQUA + member.getRole().getName() + ChatColor.GRAY + " by "  + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + ".");
            }
        }, () -> sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name or player was not found."));
    }

    @Private
    @Subcommand("force demote")
    @CommandPermission("apollo.commands.factions.force.demote")
    public static void onForceDemoteCommand(CommandSender sender, String name, OfflinePlayer target) {
        Apollo.getInstance().getFactionDao().getContaining(name).filter(faction -> faction instanceof PlayerFaction).ifPresentOrElse(faction -> {
            var member = ((PlayerFaction) faction).getMember(target.getUniqueId());
            if (member == null) {
                sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction member with that name was not found.");
            } else if (member.getRole() != Role.CAPTAIN) {
                sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not forcibly demote " + ChatColor.AQUA + member.getName() + ChatColor.GRAY + " because they are already a " + ChatColor.AQUA + member.getRole().getName() + ChatColor.GRAY + ".");
            } else {
                member.setRole(Role.MEMBER);
                ((PlayerFaction) faction).broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + member.getName() + ChatColor.GRAY + " has been forcibly demoted to a " + ChatColor.AQUA + member.getRole().getName() + ChatColor.GRAY + " by "  + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + ".");
            }
        }, () -> sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name or player was not found."));
    }

    @Private
    @Subcommand("set dtr")
    @CommandPermission("apollo.commands.factions.set.dtr")
    public static void onSetDtrCommand(CommandSender sender, String name, double value) {
        Apollo.getInstance().getFactionDao().getContaining(name).filter(faction -> faction instanceof PlayerFaction).ifPresentOrElse(faction -> {
            ((PlayerFaction) faction).setDeathsUntilRaidable(value);
            sender.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Set the faction dtr for " + ChatColor.AQUA + name + ChatColor.GRAY + " to " + ChatColor.AQUA + value + ChatColor.GRAY + ".");
        }, () -> sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name or player was not found."));
    }

}
