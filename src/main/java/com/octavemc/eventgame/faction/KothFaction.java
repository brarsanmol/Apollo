package com.octavemc.eventgame.faction;

import com.google.common.collect.ImmutableList;
import com.octavemc.eventgame.CaptureZone;
import com.octavemc.eventgame.EventType;
import com.octavemc.faction.type.ClaimableFaction;
import com.octavemc.util.BukkitUtils;
import dev.morphia.annotations.Property;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Represents a 'King of the Hill' faction.
 */
@NoArgsConstructor
public class KothFaction extends CapturableFaction {

    @Property
    private CaptureZone captureZone;

    public KothFaction(String name, String crate) {
        super(name, crate);
    }

    @Override
    public List<CaptureZone> getCaptureZones() {
        return captureZone == null ? ImmutableList.of() : ImmutableList.of(captureZone);
    }

    @Override
    public EventType getEventType() {
        return EventType.KOTH;
    }

    @Override
    public void printDetails(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(getDisplayName(sender));

        claims.forEach(claim ->
            sender.sendMessage(ChatColor.AQUA + "Location: " + ChatColor.WHITE +
                    '(' + ClaimableFaction.ENVIRONMENT_MAPPINGS.get(claim.getCenter().getWorld().getEnvironment()) + ", " + claim.getCenter().getBlockX() + " | " + claim.getCenter().getBlockZ() + ')')
        );

        if (captureZone != null) {
            long remainingCaptureMillis = captureZone.getRemainingCaptureMillis();
            long defaultCaptureMillis = captureZone.getDefaultCaptureMillis();
            if (remainingCaptureMillis > 0L && remainingCaptureMillis != defaultCaptureMillis) sender.sendMessage(ChatColor.AQUA + "Remaining Time: " + ChatColor.WHITE + DurationFormatUtils.formatDurationWords(remainingCaptureMillis, true, true));

            sender.sendMessage(ChatColor.AQUA + "Capture Delay: " + ChatColor.WHITE + captureZone.getDefaultCaptureWords());
            if (captureZone.getCappingPlayer() != null && sender.hasPermission("apollo.koth.checkcapper"))
                sender.sendMessage(ChatColor.AQUA + "Current Capper: " + ChatColor.WHITE + captureZone.getCappingPlayer());
        }

        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    /**
     * Gets the {@link CaptureZone} of this {@link KothFaction}.
     *
     * @return the {@link CaptureZone} of this {@link KothFaction}
     */
    public CaptureZone getCaptureZone() {
        return captureZone;
    }

    /**
     * Sets the {@link CaptureZone} for this {@link KothFaction}.
     *
     * @param captureZone the {@link CaptureZone} to set
     */
    public void setCaptureZone(CaptureZone captureZone) {
        this.captureZone = captureZone;
    }
}
