package com.octavemc.economy.merchants;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.user.User;
import com.octavemc.util.InventoryUtils;
import com.octavemc.util.ItemBuilder;
import com.octavemc.util.NameUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class MerchantListener implements Listener {

    public MerchantListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager && event.getRightClicked().hasMetadata("merchant")) {
            event.setCancelled(true);
            Apollo.getInstance().getMerchantDao().getByLocation(event.getRightClicked().getLocation())
                    .ifPresent(merchant -> event.getPlayer().openInventory(merchant.getInventory()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        Apollo.getInstance().getMerchantDao().get(event.getInventory().getName()).ifPresent(merchant -> {
            User user = Apollo.getInstance().getUserDao().get(event.getWhoClicked().getUniqueId()).get();
            if (event.getCurrentItem().getItemMeta().hasLore()) {
                var price = Double.valueOf(ChatColor.stripColor(
                        event.getCurrentItem().getItemMeta().getLore().get(0).substring(
                                event.getCurrentItem().getItemMeta().getLore().get(0).indexOf(Configuration.ECONOMY_SYMBOL) + 1)));

                switch (merchant.getType()) {
                    case BUY -> {
                        if (event.getWhoClicked().getInventory().firstEmpty() == -1) event.getWhoClicked().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + " Your inventory is not empty, clear a slot to purchase a item.");
                        else if (user.getBalance() < price) event.getWhoClicked().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You do not have enough money, you only have " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + user.getBalance() + ChatColor.GRAY + '.');
                        else {
                            user.setBalance((int) (user.getBalance() - price));
                            event.getWhoClicked().getInventory().addItem(new ItemBuilder(event.getCurrentItem()).loreLine(event.getCurrentItem().getItemMeta().getLore().size() - 1, null).build());
                            event.getWhoClicked().sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You bought "
                                    + ChatColor.AQUA + event.getCurrentItem().getAmount() + ChatColor.GRAY + " of "
                                    + NameUtils.getPrettyName(event.getCurrentItem().getType().name()) + ChatColor.GRAY + " for "
                                    + ChatColor.AQUA + price + ChatColor.GRAY + '.');
                        }
                    }
                    case SELL -> {
                        var quantity = Math.min(event.getCurrentItem().getAmount(), InventoryUtils.countAmount(event.getWhoClicked().getInventory(), event.getCurrentItem().getType(), event.getCurrentItem().getDurability()));
                        if (quantity <= 0) event.getWhoClicked().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You are not carrying any " + ChatColor.AQUA + NameUtils.getPrettyName(event.getCurrentItem().getType().name()) + ChatColor.GRAY + " on you.");
                        else {
                            user.setBalance((int) (user.getBalance() + ((price / event.getCurrentItem().getAmount()) * quantity)));
                            InventoryUtils.removeItem(event.getWhoClicked().getInventory(), event.getCurrentItem().getType(), event.getCurrentItem().getData().getData(), quantity);
                            event.getWhoClicked().sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You sold "
                                    + ChatColor.AQUA + quantity + ChatColor.GRAY + " of "
                                    + NameUtils.getPrettyName(event.getCurrentItem().getType().name()) + ChatColor.GRAY + " for "
                                    + ChatColor.AQUA + ((price / event.getCurrentItem().getAmount()) * quantity) + ChatColor.GRAY + '.');
                        }
                    }
                }
            }
            event.setCancelled(true);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDragEvent(InventoryDragEvent event) {
        if (Apollo.getInstance().getCrateDao().get(event.getInventory().getName()).isPresent()) event.setCancelled(true);
    }
}
