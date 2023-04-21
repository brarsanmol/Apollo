package com.octavemc.eventgame.eotw;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.type.ClaimableFaction;
import com.octavemc.util.DurationFormatter;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

/**
 * Handles the EndOfTheWorld mini-game which shrinks the border and runs a KOTH event.
 */
public class EotwHandler {

    public static final int BORDER_DECREASE_MINIMUM = 1000;
    public static final int BORDER_DECREASE_AMOUNT = 200;

    public static final long BORDER_DECREASE_TIME_MILLIS = TimeUnit.SECONDS.toMillis(20L);
    public static final int BORDER_DECREASE_TIME_SECONDS = (int) TimeUnit.MILLISECONDS.toSeconds(BORDER_DECREASE_TIME_MILLIS);
    public static final int BORDER_DECREASE_TIME_SECONDS_HALVED = BORDER_DECREASE_TIME_SECONDS / 2;
    public static final String BORDER_DECREASE_TIME_WORDS = DurationFormatUtils.formatDurationWords(BORDER_DECREASE_TIME_MILLIS, true, true);
    public static final String BORDER_DECREASE_TIME_ALERT_WORDS = DurationFormatUtils.formatDurationWords(BORDER_DECREASE_TIME_MILLIS / 2, true, true);

    public static final long EOTW_WARMUP_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    public static final int EOTW_WARMUP_WAIT_SECONDS = (int) (TimeUnit.MILLISECONDS.toSeconds(EOTW_WARMUP_WAIT_MILLIS));

    private static final long EOTW_CAPPABLE_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    private static final int WITHER_INTERVAL_SECONDS = 10;

    private EotwRunnable runnable;

    public EotwRunnable getRunnable() {
        return runnable;
    }

    /**
     * Checks if the map is currently in 'End of the World' mode.
     *
     * @return true if the map is the end of world
     */
    public boolean isEndOfTheWorld() {
        return isEndOfTheWorld(true);
    }

    /**
     * Checks if the map is currently in 'End of the World' mode.
     *
     * @param ignoreWarmup if the warmup stage is ignored
     * @return true if the map is the end of world
     */
    public boolean isEndOfTheWorld(boolean ignoreWarmup) {
        return runnable != null && (!ignoreWarmup || runnable.getElapsedMilliseconds() > 0);
    }

    /**
     * Sets if the server is currently in 'End of the World' mode.
     *
     * @param yes the value to set
     */
    public void setEndOfTheWorld(boolean yes) {
        // Don't unnecessary edit task.
        if (yes == isEndOfTheWorld(false)) {
            return;
        }

        if (yes) {
            runnable = new EotwRunnable();
            runnable.runTaskTimer(Apollo.getInstance(), 20L, 20L);
        } else {
            if (runnable != null) {
                runnable.cancel();
                runnable = null;
            }
        }
    }

    public static final class EotwRunnable extends BukkitRunnable {

        private long startStamp;
        private int elapsedSeconds;
        private double borderSize;

        public EotwRunnable() {
            this.startStamp = System.currentTimeMillis() + EOTW_WARMUP_WAIT_MILLIS;
            this.elapsedSeconds = -EOTW_WARMUP_WAIT_SECONDS;
            this.borderSize = Configuration.WORLD_BORDER;
        }

        public void handleDisconnect(Player player) {
        }

        //TODO: Cleanup these millisecond managements
        public long getMillisUntilStarting() {
            long difference = System.currentTimeMillis() - startStamp;
            return difference > 0L ? -1L : Math.abs(difference);
        }

        public long getMillisUntilCappable() {
            return EOTW_CAPPABLE_WAIT_MILLIS - getElapsedMilliseconds();
        }

        public long getElapsedMilliseconds() {
            return System.currentTimeMillis() - startStamp;
        }

        @Override
        public void run() {
            elapsedSeconds++;

            if (elapsedSeconds == 0) {
                Apollo.getInstance().getFactionDao().getCache().values().stream()
                        .filter(faction -> faction instanceof ClaimableFaction)
                        .forEach(faction -> ((ClaimableFaction) faction).removeClaims(((ClaimableFaction) faction).getClaims(),
                                Apollo.getInstance().getServer().getConsoleSender()));
                Apollo.getInstance().getServer().broadcastMessage(
                        ChatColor.GRAY + "[" + ChatColor.DARK_RED + "EOTW" + ChatColor.GRAY + "] The end of the world has begun."
                );
                cancel();
                return;
            }

            if (elapsedSeconds < 0 && elapsedSeconds >= -EOTW_WARMUP_WAIT_SECONDS) {
                Apollo.getInstance().getServer().broadcastMessage(
                            ChatColor.GRAY + "[" + ChatColor.DARK_RED + "EOTW" + ChatColor.GRAY + "] The end of the world is starting in "
                            + ChatColor.DARK_RED + DurationFormatter.getRemaining(Math.abs(elapsedSeconds) * 1000L, true, false)
                            + ChatColor.GRAY + '.'
                );
                return;
            }

            if (elapsedSeconds % BORDER_DECREASE_TIME_SECONDS == 0) {
                Apollo.getInstance().getServer().broadcastMessage(
                        Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The world border has been decreased to "
                        + ChatColor.AQUA + (borderSize - BORDER_DECREASE_AMOUNT) + ChatColor.GRAY + " blocks.");
            } else if (elapsedSeconds % BORDER_DECREASE_TIME_SECONDS_HALVED == 0) {
                Apollo.getInstance().getServer().broadcastMessage(
                        Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The world border is decreasing to "
                        + ChatColor.AQUA + (borderSize - BORDER_DECREASE_AMOUNT) + ChatColor.GRAY + " blocks.");
                Apollo.getInstance().getServer().getWorld("world").getWorldBorder().setSize((borderSize - BORDER_DECREASE_AMOUNT), BORDER_DECREASE_TIME_SECONDS);
            }
        }
    }
}
