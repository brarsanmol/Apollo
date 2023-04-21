package com.octavemc.broadcaster;

import com.octavemc.Apollo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static com.octavemc.Configuration.BROADCASTS;

public final class BroadcastManager extends BukkitRunnable implements Listener {

    private Iterator<String[]> iterator;
    
    public BroadcastManager() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
        this.iterator = BROADCASTS.listIterator();
        this.runTaskTimerAsynchronously(Apollo.getInstance(),
                TimeUnit.MINUTES.toMillis(2),
                TimeUnit.MINUTES.toMillis(2));
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        this.cancel();
    }

    @Override
    public void run() {
        if (!this.iterator.hasNext()) this.iterator = BROADCASTS.listIterator();
        for(String line : this.iterator.next())
            Apollo.getInstance().getServer().broadcastMessage(line);
    }
}
