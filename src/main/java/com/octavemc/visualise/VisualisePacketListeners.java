package com.octavemc.visualise;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.octavemc.Apollo;
import com.octavemc.util.packets.WrapperPlayClientBlockDig;
import com.octavemc.util.packets.WrapperPlayClientBlockPlace;
import org.bukkit.Location;
import org.bukkit.Material;

public class VisualisePacketListeners {

    public VisualisePacketListeners() {
        Apollo.getInstance().getProtocolManager().addPacketListener(new PacketAdapter(Apollo.getInstance(), WrapperPlayClientBlockDig.TYPE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                WrapperPlayClientBlockDig packet = new WrapperPlayClientBlockDig(event.getPacket());
                switch (packet.getStatus()) {
                    case STOP_DESTROY_BLOCK, START_DESTROY_BLOCK -> {
                        VisualBlock block = Apollo.getInstance().getVisualiseHandler().getVisualBlockAt(event.getPlayer(), packet.getLocation().toLocation(event.getPlayer().getWorld()));
                        if (block != null) {
                            event.setCancelled(true);
                            event.getPlayer().sendBlockChange(block.getLocation(), block.getBlockData().getBlockType(), block.getBlockData().getData());
                        }
                    }
                }
            }
        });

        Apollo.getInstance().getProtocolManager().addPacketListener(new PacketAdapter(Apollo.getInstance(), WrapperPlayClientBlockPlace.TYPE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                var packet = new WrapperPlayClientBlockPlace(event.getPacket());
                if (packet.getFace() == 255) return;
                if (Apollo.getInstance().getVisualiseHandler().getVisualBlockAt(event.getPlayer(), packet.getLocation().toLocation(event.getPlayer().getWorld())) != null) {
                    Location location = packet.getLocation().toLocation(event.getPlayer().getWorld());
                    switch (packet.getFace()) {
                        case 2 -> location.add(0, 0, -1);
                        case 3 -> location.add(0, 0, 1);
                        case 4 -> location.add(-1, 0, 0);
                        case 5 -> location.add(1, 0, 0);
                    }

                    if (Apollo.getInstance().getVisualiseHandler().getVisualBlockAt(event.getPlayer(), location) == null) {
                        event.setCancelled(true);
                        event.getPlayer().getPlayer().sendBlockChange(location, Material.AIR, (byte) 0);
                        //TODO: NmsUtils.resendHeldItemPacket(connection.getPlayer());
                    }
                }
            }
        });
    }
}
