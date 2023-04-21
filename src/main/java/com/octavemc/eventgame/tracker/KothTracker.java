package com.octavemc.eventgame.tracker;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.DateTimeFormats;
import com.octavemc.eventgame.CaptureZone;
import com.octavemc.eventgame.EventTimer;
import com.octavemc.eventgame.EventType;
import com.octavemc.eventgame.faction.EventFaction;
import com.octavemc.eventgame.faction.KothFaction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

@Deprecated
public class KothTracker implements EventTracker {

    /**
     * Minimum time the KOTH has to be controlled before this tracker will announce when control has been lost.
     */
    private static final long MINIMUM_CONTROL_TIME_ANNOUNCE = TimeUnit.SECONDS.toMillis(25L);

    public static final long DEFAULT_CAP_MILLIS = TimeUnit.MINUTES.toMillis(15L);

    private final Apollo plugin;

    public KothTracker(Apollo plugin) {
        this.plugin = plugin;
    }

    @Override
    public EventType getEventType() {
        return EventType.KOTH;
    }

    @Override
    public void tick(EventTimer eventTimer, EventFaction eventFaction) {
        CaptureZone captureZone = ((KothFaction) eventFaction).getCaptureZone();
        captureZone.updateScoreboardRemaining();
        long remainingMillis = captureZone.getRemainingCaptureMillis();

        if (captureZone.getCappingPlayer() != null
                && !captureZone.getCuboid().contains(captureZone.getCappingPlayer())) {
            captureZone.setCappingPlayer(null);
            onControlLoss(captureZone.getCappingPlayer(), captureZone, eventFaction);
            return;
        }

        if (remainingMillis <= 0L) { // has been captured.
            plugin.getTimerManager().getEventTimer().handleWinner(captureZone.getCappingPlayer());
            eventTimer.clearCooldown();
            return;
        }

        if (remainingMillis == captureZone.getDefaultCaptureMillis()) return;

        int remainingSeconds = (int) (remainingMillis / 1000L);
        if (remainingSeconds > 0 && remainingSeconds % 30 == 0) {
            Apollo.getInstance().getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + eventFaction.getEventType().getDisplayName()
                    + ChatColor.GRAY + "] Someone is capturing " + ChatColor.AQUA + captureZone.getDisplayName() + ChatColor.GRAY + ". "
                    + ChatColor.GRAY + '(' + ChatColor.AQUA + DateTimeFormats.KOTH_FORMAT.format(remainingMillis) + ChatColor.GRAY + ')'
            );
        }
    }

    @Override
    public void onContest(EventFaction eventFaction, EventTimer eventTimer) {
        Apollo.getInstance().getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + eventFaction.getEventType().getDisplayName()
                + ChatColor.GRAY + "] can now be captured. (" + ChatColor.AQUA + DateTimeFormats.KOTH_FORMAT.format(eventTimer.getRemaining())
                + ChatColor.GRAY + ')'
        );
    }

    @Override
    public boolean onControlTake(Player player, CaptureZone captureZone, EventFaction eventFaction) {
        player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You are now capturing " + ChatColor.AQUA + captureZone.getDisplayName() + ChatColor.GRAY + '.');
        Apollo.getInstance().getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + eventFaction.getEventType().getDisplayName()
                + ChatColor.GRAY + "] Someone is capturing " + ChatColor.AQUA + captureZone.getDisplayName() + ChatColor.GRAY + ". "
                + ChatColor.GRAY + '(' + ChatColor.AQUA + captureZone.getScoreboardRemaining() + ChatColor.GRAY + ')'
        );
        return true;
    }

    @Override
    public void onControlLoss(Player player, CaptureZone captureZone, EventFaction eventFaction) {
        player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You are no longer capturing " + ChatColor.AQUA + captureZone.getDisplayName() + ChatColor.GRAY + '.');

        // Only broadcast if the KOTH has been controlled for at least 25 seconds to prevent spam.
        // long remainingMillis = captureZone.getRemainingCaptureMillis();
        // if (remainingMillis > 0L && captureZone.getDefaultCaptureMillis() - remainingMillis > MINIMUM_CONTROL_TIME_ANNOUNCE) {
            Apollo.getInstance().getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + eventFaction.getEventType().getDisplayName()
                    + ChatColor.GRAY + "] " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + " is no longer capturing "
                    + ChatColor.AQUA + captureZone.getDisplayName() + ChatColor.GRAY + '.'
                    + ChatColor.GRAY + " (" + ChatColor.AQUA + captureZone.getScoreboardRemaining() + ChatColor.GRAY + ')'
            );
//        }
    }

    @Override
    public void stopTiming() {

    }
}
