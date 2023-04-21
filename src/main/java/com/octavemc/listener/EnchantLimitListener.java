package com.octavemc.listener;

import com.google.common.collect.ImmutableMap;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import net.minecraft.server.v1_8_R3.ItemArmor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//import org.bukkit.event.inventory.PrepareAnvilRepairEvent;

/**
 * Listener that limits the maximum {@link Enchantment} levels for {@link ItemStack}s.
 */
public class EnchantLimitListener implements Listener {

    private final ImmutableMap<Material, ItemArmor.EnumArmorMaterial> ITEM_TOOL_MAPPING = /*TODO:Maps.immutableEnumMap*/(ImmutableMap.of(
            Material.IRON_INGOT, ItemArmor.EnumArmorMaterial.IRON,
            Material.GOLD_INGOT, ItemArmor.EnumArmorMaterial.GOLD,
            Material.DIAMOND, ItemArmor.EnumArmorMaterial.DIAMOND
    ));

    private final ImmutableMap<Material, ItemArmor.EnumArmorMaterial> ITEM_ARMOUR_MAPPING = /*TODO:Maps.immutableEnumMap*/(ImmutableMap.of(
            Material.IRON_INGOT, ItemArmor.EnumArmorMaterial.IRON,
            Material.GOLD_INGOT, ItemArmor.EnumArmorMaterial.GOLD,
            Material.DIAMOND, ItemArmor.EnumArmorMaterial.DIAMOND
    ));

    private final Apollo plugin;

    public EnchantLimitListener(Apollo plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEnchantItem(EnchantItemEvent event) {
        Map<Enchantment, Integer> adding = event.getEnchantsToAdd();
        Iterator<Map.Entry<Enchantment, Integer>> iterator = adding.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Enchantment, Integer> entry = iterator.next();
            Enchantment enchantment = entry.getKey();
            int maxLevel = Configuration.getEnchantmentLimit(enchantment);
            if (entry.getValue() > maxLevel) {
                if (maxLevel > 0) {
                    adding.put(enchantment, maxLevel);
                } else {
                    iterator.remove();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            for (ItemStack drop : event.getDrops()) {
                this.validateIllegalEnchants(drop);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (caught instanceof Item) {
            validateIllegalEnchants(((Item) caught).getItemStack());
        }
    }

    /* TODO: BROKEN: Non existant event
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPrepareAnvilRepair(PrepareAnvilRepairEvent event) {
        ItemStack first = event.getFirst();
        ItemStack second = event.getSecond();

        // Some NMS to make sure that if the player is using the respective ingot to repair,
        // the player will be allowed to repair.
        if (first != null && first.getType() != Material.AIR && second != null && second.getType() != Material.AIR) {
            Object firstItemObj = net.minecraft.server.v1_7_R4.Item.REGISTRY.a(first.getTypeId());
            if (firstItemObj instanceof net.minecraft.server.v1_7_R4.Item) { // better safe than sorry
                net.minecraft.server.v1_7_R4.Item nmsFirstItem = (net.minecraft.server.v1_7_R4.Item) firstItemObj;
                if (nmsFirstItem instanceof ItemTool) {
                    if (ITEM_TOOL_MAPPING.get(second.getType()) == ((ItemTool) nmsFirstItem).i()) {
                        return;
                    }
                    // ItemSwords don't extend ItemTool, NMS </3
                } else if (nmsFirstItem instanceof ItemSword) {
                    EnumToolMaterial comparison = ITEM_TOOL_MAPPING.get(second.getType());
                    if (comparison != null && comparison.e() == nmsFirstItem.c()) {
                        return;
                    }
                } else if (nmsFirstItem instanceof ItemArmor) {
                    if (ITEM_ARMOUR_MAPPING.get(second.getType()) == ((ItemArmor) nmsFirstItem).m_()) {
                        return;
                    }
                }
            }
        }

        HumanEntity repairer = event.getRepairer();
        if (repairer instanceof Player) {
            validateIllegalEnchants(event.getResult());
        }
    } */

    /**
     * Validates the {@link Enchantment}s of a {@link ItemStack}, removing any disallowed ones.
     *
     * @param stack the {@link ItemStack} to validate
     * @return true if was changed during validation
     */
    private boolean validateIllegalEnchants(ItemStack stack) {
        boolean updated = false;
        if (stack != null && stack.getType() != Material.AIR) {
            ItemMeta meta = stack.getItemMeta();
            Set<Map.Entry<Enchantment, Integer>> entries;

            // Have to use this for books.
            if (meta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) meta;
                entries = enchantmentStorageMeta.getStoredEnchants().entrySet();
                for (Map.Entry<Enchantment, Integer> entry : entries) {
                    Enchantment enchantment = entry.getKey();
                    int maxLevel = Configuration.getEnchantmentLimit(enchantment);
                    if (entry.getValue() > maxLevel) {
                        updated = true;
                        if (maxLevel > 0) {
                            enchantmentStorageMeta.addStoredEnchant(enchantment, maxLevel, false);
                        } else {
                            enchantmentStorageMeta.removeStoredEnchant(enchantment);
                        }
                    }
                }

                // Re-apply the ItemMeta.
                stack.setItemMeta(meta);
            } else {
                entries = stack.getEnchantments().entrySet();
                for (Map.Entry<Enchantment, Integer> entry : entries) {
                    Enchantment enchantment = entry.getKey();
                    int maxLevel = Configuration.getEnchantmentLimit(enchantment);
                    if (entry.getValue() > maxLevel) {
                        updated = true;
                        stack.removeEnchantment(enchantment);
                        if (maxLevel > 0) {
                            stack.addEnchantment(enchantment, maxLevel);
                        }
                    }
                }
            }
        }

        return updated;
    }
}
