package com.octavemc.tablist;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.octavemc.Apollo;
import com.octavemc.util.packets.WrapperPlayServerPlayerInfo;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.stream.Collectors;

public class TablistTask extends BukkitRunnable {

    public TablistTask() {
        this.runTaskTimerAsynchronously(Apollo.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        Apollo.getInstance().getTablistManager().getTablists().values().forEach(tablist -> {
                WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(EnumWrappers.PlayerInfoAction.ADD_PLAYER,
                        tablist.getSlots().stream()
                        .filter(TablistSlot::isShouldUpdate)
                        .peek(slot -> slot.setShouldUpdate(false))
                        .map(slot -> new PlayerInfoData(
                                slot.getProfile(),
                                slot.getLatency(),
                                EnumWrappers.NativeGameMode.NOT_SET,
                                WrappedChatComponent.fromText(slot.getText())))
                        .collect(Collectors.toList()));
                packet.sendPacket(tablist.getPlayer());
        });
    }
}
