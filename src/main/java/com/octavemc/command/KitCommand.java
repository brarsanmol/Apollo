package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.octavemc.Apollo;
import com.octavemc.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

@CommandAlias("kit")
public class KitCommand extends BaseCommand {

    public KitCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @Subcommand("diamond|d")
    public void onDiamondCommand(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.DIAMOND_SWORD).enchant(Enchantment.DAMAGE_ALL, 2).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().addItem(new ItemBuilder(Material.ENDER_PEARL, 16).build());
        player.getInventory().setHelmet(new ItemBuilder(Material.DIAMOND_HELMET).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setChestplate(new ItemBuilder(Material.DIAMOND_CHESTPLATE).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setLeggings(new ItemBuilder(Material.DIAMOND_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setBoots(new ItemBuilder(Material.DIAMOND_BOOTS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.PROTECTION_FALL, 3).enchant(Enchantment.DURABILITY, 2).build());
        player.getInventory().addItem(new ItemBuilder(new Potion(PotionType.INSTANT_HEAL, 2, true).toItemStack(33)).build());
        player.getInventory().addItem(new ItemBuilder(Material.POTION, 4, (byte) 8226).build());
        player.getInventory().addItem(new ItemBuilder(Material.COOKED_BEEF, 64).build());
    }

    @Subcommand("bard|b")
    public void onBardCommand(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.DIAMOND_SWORD).enchant(Enchantment.DAMAGE_ALL, 2).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().addItem(new ItemBuilder(Material.ENDER_PEARL, 16).build());
        player.getInventory().setHelmet(new ItemBuilder(Material.GOLD_HELMET).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setChestplate(new ItemBuilder(Material.GOLD_CHESTPLATE).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setLeggings(new ItemBuilder(Material.GOLD_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setBoots(new ItemBuilder(Material.GOLD_BOOTS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.PROTECTION_FALL, 3).enchant(Enchantment.DURABILITY, 2).build());
        player.getInventory().addItem(new ItemBuilder(new Potion(PotionType.INSTANT_HEAL, 2, true).toItemStack(29)).build());
        player.getInventory().addItem(new ItemBuilder(Material.IRON_INGOT, 64).build());
        player.getInventory().addItem(new ItemBuilder(Material.SUGAR, 64).build());
        player.getInventory().addItem(new ItemBuilder(Material.BLAZE_POWDER, 64).build());
        player.getInventory().addItem(new ItemBuilder(Material.FEATHER, 64).build());
        player.getInventory().addItem(new ItemBuilder(Material.GHAST_TEAR, 64).build());
        player.getInventory().addItem(new ItemBuilder(Material.COOKED_BEEF, 64).build());
    }

    @Subcommand("archer|a")
    public void onArcherCommand(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.DIAMOND_SWORD).enchant(Enchantment.DAMAGE_ALL, 2).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().addItem(new ItemBuilder(Material.ENDER_PEARL, 16).build());
        player.getInventory().addItem(new ItemBuilder(Material.BOW).enchant(Enchantment.ARROW_DAMAGE, 1).enchant(Enchantment.ARROW_FIRE, 1).enchant(Enchantment.ARROW_INFINITE, 1).enchant(Enchantment.ARROW_KNOCKBACK, 1).build());
        player.getInventory().addItem(new ItemBuilder(Material.ARROW, 1).build());
        player.getInventory().setHelmet(new ItemBuilder(Material.LEATHER_HELMET).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setChestplate(new ItemBuilder(Material.LEATHER_CHESTPLATE).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setLeggings(new ItemBuilder(Material.LEATHER_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setBoots(new ItemBuilder(Material.LEATHER_BOOTS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.PROTECTION_FALL, 3).enchant(Enchantment.DURABILITY, 2).build());
        player.getInventory().addItem(new ItemBuilder(new Potion(PotionType.INSTANT_HEAL, 2, true).toItemStack(32)).build());
        player.getInventory().addItem(new ItemBuilder(Material.COOKED_BEEF, 64).build());
    }

    @Subcommand("rouge|r")
    public void onRougeCommand(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.DIAMOND_SWORD).enchant(Enchantment.DAMAGE_ALL, 2).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().addItem(new ItemBuilder(Material.ENDER_PEARL, 16).build());
        player.getInventory().addItem(new ItemBuilder(Material.GOLD_SWORD, 9).build());
        player.getInventory().setHelmet(new ItemBuilder(Material.CHAINMAIL_HELMET).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setChestplate(new ItemBuilder(Material.CHAINMAIL_CHESTPLATE).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setLeggings(new ItemBuilder(Material.CHAINMAIL_LEGGINGS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.DURABILITY, 3).build());
        player.getInventory().setBoots(new ItemBuilder(Material.CHAINMAIL_BOOTS).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).enchant(Enchantment.PROTECTION_FALL, 3).enchant(Enchantment.DURABILITY, 2).build());
        player.getInventory().addItem(new ItemBuilder(new Potion(PotionType.INSTANT_HEAL, 2, true).toItemStack(29)).build());
        player.getInventory().addItem(new ItemBuilder(Material.COOKED_BEEF, 64).build());
    }

}
