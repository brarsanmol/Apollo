package com.octavemc.timer;

import com.octavemc.Apollo;
import com.octavemc.eventgame.EventTimer;
import com.octavemc.timer.type.*;
import lombok.Getter;
import org.bukkit.event.Listener;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class TimerManager implements Listener {

    private final CombatTimer combatTimer;
    private final LogoutTimer logoutTimer;
    private final EnderPearlTimer enderpearlTimer;
    private final EventTimer eventTimer;
    private final GoldenAppleTimer goldenAppleTimer;
    private final SuperGoldenAppleTimer superGoldenAppleTimer;
    private final TotemOfUndyingTimer totemOfUndyingTimer;
    private final InvincibilityTimer invincibilityTimer;
    private final PvpClassWarmupTimer pvpClassWarmupTimer;
    private final StuckTimer stuckTimer;
    private final TeleportTimer teleportTimer;

    private final Set<Timer> timers = new LinkedHashSet<>();

    public TimerManager() {
        registerTimer(enderpearlTimer = new EnderPearlTimer());
        registerTimer(logoutTimer = new LogoutTimer());
        registerTimer(goldenAppleTimer = new GoldenAppleTimer());
        registerTimer(superGoldenAppleTimer = new SuperGoldenAppleTimer());
        registerTimer(totemOfUndyingTimer = new TotemOfUndyingTimer());
        registerTimer(stuckTimer = new StuckTimer());
        registerTimer(invincibilityTimer = new InvincibilityTimer());
        registerTimer(combatTimer = new CombatTimer());
        registerTimer(teleportTimer = new TeleportTimer());
        registerTimer(eventTimer = new EventTimer());
        registerTimer(pvpClassWarmupTimer = new PvpClassWarmupTimer());
    }

    public void registerTimer(Timer timer) {
        timers.add(timer);
        if (timer instanceof Listener) {
            Apollo.getInstance().getServer().getPluginManager().registerEvents((Listener) timer, Apollo.getInstance());
        }
    }

    public void unregisterTimer(Timer timer) {
        timers.remove(timer);
    }

    public Timer getTimer(String identifier) {
        return this.timers.stream().filter(timer -> timer.getName().equalsIgnoreCase(identifier)).findFirst().orElse(null);
    }
}
