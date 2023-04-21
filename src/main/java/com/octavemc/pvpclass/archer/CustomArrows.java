package com.octavemc.pvpclass.archer;

import com.octavemc.util.ItemBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@AllArgsConstructor
@Getter
public enum CustomArrows {

    POISONOUS_ARROW(new ItemBuilder(Material.ARROW, 4)
            .displayName(ChatColor.DARK_GREEN + "Poisonous Arrow")
            .loreLine(ChatColor.WHITE + "Applies a poison effect to a hit target for 16 seconds.")
            .build(),
            (short) 8196,
            new PotionEffect(PotionEffectType.POISON, 12 * 20, 0)),
    WEAKNESS_ARROW(new ItemBuilder(Material.ARROW, 4)
            .displayName(ChatColor.DARK_PURPLE + "Weakness Arrow")
            .lore(
                    ChatColor.WHITE + "Applies a weakness effect to a hit target for 16 seconds."
            )
            .build(),
            (short) 8232,
            new PotionEffect(PotionEffectType.WEAKNESS, 22 * 20, 0)),
    NAUSEA_ARROW(new ItemBuilder(Material.ARROW, 4)
            .displayName(ChatColor.DARK_AQUA + "Nausea Arrow")
            .lore(
                    ChatColor.WHITE + "Applies a nausea effect to a hit target for 12 seconds."
            )
            .build(),
            (short) 8264,
            new PotionEffect(PotionEffectType.CONFUSION, 16 * 20, 0)),
    SLOWNESS_ARROW(new ItemBuilder(Material.ARROW, 4)
            .displayName(ChatColor.GRAY + "Slowness Arrow")
            .lore(
                    ChatColor.WHITE + "Applies a slowness effect to a hit target for 16 seconds."
            )
            .build(),
            (short) 8234,
            new PotionEffect(PotionEffectType.SLOW, 16 * 20, 0));

    private ItemStack stack;
    private short potion;
    private PotionEffect effect;


}
