package com.octavemc.pvpclass.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public final class PlayerArmorUnequipEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;

    private final ItemStack itemStack;

    public PlayerArmorUnequipEvent(Player player, ItemStack itemStack) {
        this.player = player;
        this.itemStack = itemStack;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}