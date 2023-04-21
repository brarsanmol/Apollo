package com.octavemc.eventgame.koth;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.eventgame.faction.EventFaction;
import com.octavemc.eventgame.faction.KothFaction;
import com.octavemc.faction.type.Faction;
import com.octavemc.util.BukkitUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * Command used to handle KingOfTheHills.
 */
@CommandAlias("koth")
@CommandPermission("apollo.commands.koth")
public class KothCommand extends BaseCommand {

    private static final String TIME_UNTIL_PATTERN = "d'd' H'h' mm'm'";

    private static FastDateFormat headingTimeFormat;
    private static FastDateFormat eachKothTimeFormat;

    public KothCommand() {
        this.headingTimeFormat = FastDateFormat.getInstance("EEE FF h:mma (z)", TimeZone.getDefault(), Locale.ENGLISH);
        this.eachKothTimeFormat = FastDateFormat.getInstance("EEE dd '" + Matcher.quoteReplacement("&b") + "'(h:mma)", TimeZone.getDefault(), Locale.ENGLISH);
        Apollo.getInstance().getCommandManager().registerCommand(this);
        Apollo.getInstance().getCommandManager().getCommandCompletions().registerAsyncCompletion("koths", context -> Apollo.getInstance().getFactionDao().getCache().values().stream().filter(faction -> faction instanceof KothFaction).map(Faction::getName).collect(Collectors.toList()));
    }

    @HelpCommand
    public static void onHelpCommand(CommandHelp help) {
        help.showHelp();
        // sender.sendMessage(ChatColor.GRAY + "/fac show <kothName> - View information about a KOTH.");
    }

    @Subcommand("create")
    public static void onCreateCommand(CommandSender sender, String identifier, String crate) {
        if (Apollo.getInstance().getFactionDao().getByName(identifier).isPresent()) {
            sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A event / faction with that name already exists!");
        } else if (Apollo.getInstance().getCrateDao().get(crate).isEmpty()) {
            sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A crate with that identifier does not exist.");
        } else if (Apollo.getInstance().getFactionDao().create(new KothFaction(identifier, crate), sender)) {
            sender.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Created a KoTH with the name  " + ChatColor.AQUA + identifier + ChatColor.GRAY + '.');
        }
    }

    @Subcommand("start")
    public static void onStartCommand(CommandSender sender) {
    }

    @Subcommand("schedule")
    public static void onScheduleCommand(CommandSender sender) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        int currentDay = now.getDayOfYear();

        Map<LocalDateTime, String> scheduleMap = Apollo.getInstance().getEventScheduler().getSchedule();
        List<String> shownEvents = new ArrayList<>();
        for (Map.Entry<LocalDateTime, String> entry : scheduleMap.entrySet()) {
            LocalDateTime scheduleDateTime = entry.getKey();
            if (scheduleDateTime.isAfter(now)) { // only show the events that haven't been scheduled yet.
                int dayDifference = scheduleDateTime.getDayOfYear() - currentDay;
                if (dayDifference > 1) {
                    continue; // only show events today or tomorrow.
                }

                ChatColor colour = dayDifference == 0 ? ChatColor.GREEN : ChatColor.AQUA;
                long remainingMillis = now.until(scheduleDateTime, ChronoUnit.MILLIS);
                shownEvents.add("  " + colour + WordUtils.capitalize(entry.getValue()) + ": " + ChatColor.YELLOW +
                        ChatColor.translateAlternateColorCodes('&', eachKothTimeFormat.format(remainingMillis)) +
                        ChatColor.GRAY + " - " + ChatColor.GOLD + DurationFormatUtils.formatDuration(remainingMillis, TIME_UNTIL_PATTERN));
            }
        }

        if (shownEvents.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "There are no event schedules defined.");
        }

        sender.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(ChatColor.GRAY + "Server time is currently " + ChatColor.WHITE + headingTimeFormat.format(System.currentTimeMillis()) + ChatColor.GRAY + '.');
        sender.sendMessage(shownEvents.toArray(new String[shownEvents.size()]));
        sender.sendMessage(ChatColor.GRAY + "For more info about King of the Hill, use " + ChatColor.WHITE + "/koth help" + ChatColor.GRAY + '.');
        sender.sendMessage(ChatColor.GRAY + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    @Subcommand("set cap-delay")
    public static void onSetCapDelay(CommandSender sender, String identifier, int delay) {
        Apollo.getInstance().getFactionDao().getByName(identifier)
                .filter(faction -> faction instanceof EventFaction)
                .ifPresentOrElse(faction -> {
                    if (((KothFaction) faction).getCaptureZone() == null) {
                        sender.sendMessage(ChatColor.RED + faction.getDisplayName(sender) + ChatColor.RED + " does not have a capture zone.");
                    } else {
                        if (((KothFaction) faction).getCaptureZone().isActive() && delay < ((KothFaction) faction).getCaptureZone().getRemainingCaptureMillis()) {
                            ((KothFaction) faction).getCaptureZone().setRemainingCaptureMillis(delay);
                        }
                        ((KothFaction) faction).getCaptureZone().setDefaultCaptureMillis(delay);
                        sender.sendMessage(ChatColor.YELLOW + "Set the capture delay of KOTH arena " +
                                ChatColor.WHITE + faction.getDisplayName(sender) + ChatColor.YELLOW + " to " +
                                ChatColor.WHITE + DurationFormatUtils.formatDurationWords(delay, true, true) + ChatColor.WHITE + '.');
                    }
        }, () -> sender.sendMessage(ChatColor.RED + "There is not a KOTH arena named '" + identifier + "'."));
    }
}
