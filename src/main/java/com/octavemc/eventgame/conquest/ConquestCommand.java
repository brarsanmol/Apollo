package com.octavemc.eventgame.conquest;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.eventgame.CaptureZone;
import com.octavemc.eventgame.EventType;
import com.octavemc.eventgame.faction.ConquestFaction;
import com.octavemc.eventgame.tracker.ConquestTracker;
import com.octavemc.faction.claim.Claim;
import com.octavemc.faction.claim.ClaimHandler;
import com.octavemc.faction.claim.ClaimSelection;
import com.octavemc.faction.type.PlayerFaction;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

@CommandAlias("conquest")
@CommandPermission("apollo.commands.conquest")
public class ConquestCommand extends BaseCommand {

    public ConquestCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
        Apollo.getInstance().getCommandManager().getCommandCompletions().registerStaticCompletion("conquest-zones", Arrays.stream(ConquestFaction.ConquestZone.values()).map(zone -> zone.name()).collect(Collectors.toList()));
    }

    @Subcommand("set zone")
    @CommandPermission("apollo.commands.conquest.set.zone")
    public static void onSetZone(Player player, String identifier, ConquestFaction.ConquestZone zone) {
        Apollo.getInstance().getFactionDao().getByName(identifier)
                .filter(faction -> faction instanceof ConquestFaction && faction.getName().equalsIgnoreCase(identifier))
                .ifPresentOrElse(faction -> {
                    ClaimSelection selection = Apollo.getInstance().getClaimHandler().getClaimSelectionMap().get(player.getUniqueId());
                    Claim claim;
                    if (selection == null || !selection.hasBothPositionsSet()) {
                        player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You have not set both positions of this claim selection.");
                    } else if ((claim = selection.toClaim(faction)) != null) {
                        ((ConquestFaction) faction).setZone(zone, new CaptureZone(zone.getName(), zone.getColor().toString(), claim, ConquestTracker.DEFAULT_CAP_MILLIS));
                        Apollo.getInstance().getClaimHandler().clearClaimSelection(player);
                        player.getInventory().remove(ClaimHandler.EVENT_CLAIM_WAND);
                        player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Set capture zone for the event" + ChatColor.AQUA + faction.getName() + ChatColor.GRAY + " at " + ChatColor.GRAY + '(' + ChatColor.AQUA + claim.getCenter().getBlockX() + ChatColor.GRAY + ", " + ChatColor.AQUA + claim.getCenter().getBlockZ() + ChatColor.GRAY + ").");
                    }
                }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name does not exist!"));
    }

    @Subcommand("set points")
    @CommandPermission("apollo.commands.conquest.set.points")
    public static void onSetPoints(CommandSender sender, String name, int points) {
        Apollo.getInstance().getFactionDao().getByName(name).filter(faction -> faction instanceof PlayerFaction).ifPresentOrElse(faction -> {
            if (points > Configuration.CONQUEST_REQUIRED_VICTORY_POINTS)
                sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + "The maximum points for conquest is" + ChatColor.AQUA + Configuration.CONQUEST_REQUIRED_VICTORY_POINTS);
            else
                ((ConquestTracker) EventType.CONQUEST.getEventTracker()).setPoints((PlayerFaction) faction, points);
        }, () -> sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A faction with that name does not exist!"));
    }

}
