package com.octavemc.eventgame;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.DateTimeFormats;
import com.octavemc.eventgame.faction.ConquestFaction;
import com.octavemc.eventgame.faction.EventFaction;
import com.octavemc.eventgame.faction.KothFaction;
import com.octavemc.eventgame.tracker.KothTracker;
import com.octavemc.faction.claim.Claim;
import com.octavemc.faction.claim.ClaimHandler;
import com.octavemc.faction.claim.ClaimSelection;
import com.octavemc.faction.type.Faction;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

@CommandAlias("event")
@CommandPermission("apollo.commands.event")
public class EventCommand extends BaseCommand {

    public EventCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
        Apollo.getInstance().getCommandManager().getCommandCompletions().registerStaticCompletion("event-types", Arrays.stream(EventType.values()).map(EventType::name).collect(Collectors.toUnmodifiableList()));
    }

    @Subcommand("create")
    @CommandPermission("apollo.commands.event.create")
    @Description("Create a event faction.")
    public static void onCreateCommand(CommandSender sender, String identifier, String crate, String type) {
        if (Apollo.getInstance().getFactionDao().getByName(identifier).isPresent()) {
            sender.sendMessage(ChatColor.RED + "There is already a faction named " + identifier + '.');
        } else if (Apollo.getInstance().getCrateDao().get(crate).isEmpty()) {
            sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A crate with that identifier does not exist.");
        } else if (type.equalsIgnoreCase("koth") &&
                Apollo.getInstance().getFactionDao().create(new KothFaction(identifier, crate), sender)) {
            sender.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Created a KoTH with the name " + ChatColor.AQUA + identifier + ChatColor.GRAY + '.');
        } else if (type.equalsIgnoreCase("conquest") &&
                Apollo.getInstance().getFactionDao().create(new ConquestFaction(identifier, crate), sender)) {
            sender.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Created Conquest with the name " + ChatColor.AQUA + identifier + ChatColor.GRAY + '.');
        }
    }

    @Subcommand("delete")
    @Syntax("&c&lERROR! &c/event delete <identifier>")
    @CommandPermission("apollo.commands.event.delete")
    @Description("Delete a event faction.")
    public static void onDeleteCommand(CommandSender sender, String identifier) {
        Apollo.getInstance().getFactionDao().getByName(identifier)
                .filter(faction -> faction instanceof EventFaction)
                .ifPresentOrElse(faction -> {
                    if (Apollo.getInstance().getFactionDao().remove(faction, sender)) {
                        sender.sendMessage(ChatColor.YELLOW + "Deleted event faction " + ChatColor.WHITE + faction.getDisplayName(sender) + ChatColor.YELLOW + '.');
                    }
                }, () -> sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A event with that name does not exist."));
    }

    @Subcommand("start")
    @Syntax("&c&lERROR! &c/event start <identifier>")
    @CommandPermission("apollo.commands.event.start")
    @Description("Start a event.")
    public static void onStartCommand(CommandSender sender, String identifier) {
        Apollo.getInstance().getFactionDao().getByName(identifier)
                .filter(faction -> faction instanceof EventFaction)
                .ifPresentOrElse(faction -> {
                    if (Apollo.getInstance().getTimerManager().getEventTimer().tryContesting((EventFaction) faction, sender)) {
                        sender.sendMessage(ChatColor.YELLOW + "Successfully contested " + faction.getName() + '.');
                    }
                }, () -> sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A event with that name does not exist."));
    }

    @Subcommand("cancel")
    @CommandPermission("apollo.commands.event.cancel")
    @Description("Cancel a event.")
    public static void onCancelCommand(CommandSender sender) {
        EventTimer timer = Apollo.getInstance().getTimerManager().getEventTimer();
        if (!Apollo.getInstance().getTimerManager().getEventTimer().clearCooldown()) {
            sender.sendMessage(ChatColor.RED + "There is not a running event.");
        } else {
            Apollo.getInstance().getServer().broadcastMessage(sender.getName() + ChatColor.YELLOW + " has cancelled " + (timer.getEventFaction() == null ? "the active event" : timer.getEventFaction().getName() + ChatColor.YELLOW) + ".");
        }
    }

    @Subcommand("rename")
    @Syntax("&c&lERROR! &c/event rename <identifier> <new-identifier>")
    @CommandPermission("apollo.commands.event.rename")
    @Description("Rename a event faction.")
    public static void onRenameCommand(CommandSender sender, String identifier, String target) {
        Faction faction = Apollo.getInstance().getFactionDao().getByName(identifier).get();

        if (!(faction instanceof EventFaction)) {
            sender.sendMessage(ChatColor.RED + "There is not an event faction named '" + identifier + "'.");
        } else if (Apollo.getInstance().getFactionDao().getByName(target).isPresent()) {
            sender.sendMessage(ChatColor.RED + "There is already a faction named " + target + '.');
        } else {
            faction.setName(target);
            sender.sendMessage(ChatColor.YELLOW + "Renamed event " + ChatColor.WHITE + identifier + ChatColor.YELLOW + " to " + ChatColor.WHITE + target + ChatColor.YELLOW + '.');
        }
    }

    @Subcommand("uptime")
    @CommandPermission("apollo.commands.event.uptime")
    @Description("Check how long a event has been running.")
    public static void onUptimeCommand(CommandSender sender) {
        EventTimer timer = Apollo.getInstance().getTimerManager().getEventTimer();

        if (timer.getRemaining() <= 0L) {
            sender.sendMessage(ChatColor.RED + "There is not a running event.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Up-time of " + timer.getName() + " timer" +
                    (timer.getEventFaction().getName() == null ? "" : ": " + ChatColor.BLUE + '(' + timer.getEventFaction().getDisplayName(sender) + ChatColor.BLUE + ')') +
                    ChatColor.YELLOW + " is " + ChatColor.GRAY + DurationFormatUtils.formatDurationWords(timer.getUptime(), true, true) + ChatColor.YELLOW + ", started at " +
                    ChatColor.GOLD + DateTimeFormats.HR_MIN_AMPM_TIMEZONE.format(timer.getStartStamp()) + ChatColor.YELLOW + '.');
        }
    }

    @Subcommand("set crate")
    @CommandPermission("apollo.commands.event.set.crate")
    @Description("Set the crate for winning a event.")
    public static void onSetCrateCommand(CommandSender sender, String identifier, String crate) {
        //TODO: Move crate variable into CapturableFaction.
        // We can't currently do this because the CapturableFaction is not what is serialized.
        Apollo.getInstance().getFactionDao().getByName(identifier)
                .filter(faction -> faction instanceof ConquestFaction || faction instanceof KothFaction)
                .ifPresentOrElse(faction -> {
                    if (Apollo.getInstance().getCrateDao().get(identifier).isEmpty()) sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A crate with that identifier does not exist.");
                    else if (faction instanceof KothFaction koth) koth.setCrate(crate);
                    else if (faction instanceof ConquestFaction conquest) conquest.setCrate(crate);
                    sender.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "The crate for the event " + ChatColor.AQUA + identifier + ChatColor.GRAY + " has been set to " + ChatColor.AQUA + crate + ChatColor.GRAY + '.');
                }, () -> sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A event with that name does not exist."));
    }

    @Subcommand("wand")
    @CommandPermission("apollo.commands.event.wand")
    @Description("Get a event claiming wand.")
    public static void onWandCommand(Player player) {
        if (player.getInventory().contains(ClaimHandler.EVENT_CLAIM_WAND)
                || player.getInventory().contains(ClaimHandler.CLAIM_WAND)
                || player.getInventory().contains(ClaimHandler.SUBCLAIM_WAND)) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You already have a claiming wand in your inventory.");
        } else if (!player.getInventory().addItem(ClaimHandler.EVENT_CLAIM_WAND).isEmpty()) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your inventory is full.");
        } else {
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Event claiming wand added to inventory, read the item to understand how to claim.");
        }
    }


    @Subcommand("set claim")
    @CommandPermission("apollo.commands.event.set.claim")
    @Description("Claim for a event faction.")
    public static void onSetClaimCommand(Player player, String identifier) {
        Apollo.getInstance().getFactionDao().getByName(identifier)
                .filter(faction -> faction instanceof KothFaction || faction instanceof ConquestFaction && faction.getName().equalsIgnoreCase(identifier))
                .ifPresentOrElse(faction -> {
                    ClaimSelection selection = Apollo.getInstance().getClaimHandler().getClaimSelectionMap().get(player.getUniqueId());
                    if (selection == null || !selection.hasBothPositionsSet()) {
                        player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You have not set both positions of this claim selection.");
                    } else {
                        ((EventFaction) faction).setClaim(selection.toClaim(faction), player);
                        Apollo.getInstance().getClaimHandler().clearClaimSelection(player);
                        player.getInventory().remove(ClaimHandler.EVENT_CLAIM_WAND);
                        player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Claimed land for the event " + ChatColor.AQUA + faction.getName() + ChatColor.GRAY + " at " + ChatColor.GRAY + '(' + ChatColor.AQUA + ((EventFaction) faction).getClaims().get(0).getCenter().getBlockX() + ChatColor.GRAY + ", " + ChatColor.AQUA + ((EventFaction) faction).getClaims().get(0).getCenter().getBlockZ() + ChatColor.GRAY + ").");
                    }
                }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A event with that name does not exist."));
    }

    @Subcommand("set zone")
    @CommandPermission("apollo.commands.event.set.zone")
    @Description("Claim the capture zone for a event faction.")
    public static void onSetZoneCommand(Player player, String identifier) {
        Apollo.getInstance().getFactionDao().getByName(identifier)
                .filter(faction -> faction instanceof KothFaction || faction instanceof ConquestFaction && faction.getName().equalsIgnoreCase(identifier))
                .ifPresentOrElse(faction -> {
                    ClaimSelection selection = Apollo.getInstance().getClaimHandler().getClaimSelectionMap().get(player.getUniqueId());
                    Claim claim = selection.toClaim(faction);
                    if (selection == null || !selection.hasBothPositionsSet()) {
                        player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You have not set both positions of this claim selection.");
                    } else {
                        ((KothFaction) faction).setCaptureZone(new CaptureZone(faction.getName(), claim, KothTracker.DEFAULT_CAP_MILLIS));
                        Apollo.getInstance().getClaimHandler().clearClaimSelection(player);
                        player.getInventory().remove(ClaimHandler.EVENT_CLAIM_WAND);
                        player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Set capture zone for the event " + ChatColor.AQUA + faction.getName() + ChatColor.GRAY + " at " + ChatColor.GRAY + '(' + ChatColor.AQUA + claim.getCenter().getBlockX() + ChatColor.GRAY + ", " + ChatColor.AQUA + claim.getCenter().getBlockZ() + ChatColor.GRAY + ").");
                    }
                }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A event with that name does not exist."));
    }
}
