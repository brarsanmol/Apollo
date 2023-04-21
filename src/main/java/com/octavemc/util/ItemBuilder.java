package com.octavemc.util;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

public class ItemBuilder {

    private ItemStack stack;
    private ItemMeta meta;

    /**
     * Creates a new instance with a given material
     * and a default quantity of 1.
     *
     * @param material the material to create from
     */
    public ItemBuilder(Material material) {
        this(material, 1);
    }

    /**
     * Creates a new instance with a given material and quantity.
     *
     * @param material the material to create from
     * @param amount   the quantity to build with
     */
    public ItemBuilder(Material material, int amount) {
        this(material, amount, (byte) 0);
    }

    /**
     * Creates a new instance with a given {@link ItemStack}.
     *
     * @param stack the stack to create from
     */
    public ItemBuilder(ItemStack stack) {
        Preconditions.checkNotNull(stack, "ItemStack cannot be null");
        this.stack = stack;
    }

    /**
     * Creates a new instance with a given material, quantity and data.
     *
     * @param material the material to create from
     * @param amount   the quantity to build with
     * @param data     the data to build with
     */
    public ItemBuilder(Material material, int amount, byte data) {
        Preconditions.checkNotNull(material, "Material cannot be null");
        Preconditions.checkArgument(amount > 0, "Amount must be positive");
        this.stack = new ItemStack(material, amount, data);
    }

    /**
     * Sets the display name of this item builder.
     *
     * @param name the display name to set
     * @return this instance
     */
    public ItemBuilder displayName(String name) {
        if (this.meta == null) {
            this.meta = stack.getItemMeta();
        }

        meta.setDisplayName(name);
        return this;
    }

    /**
     * Adds a line to the lore of this builder at a specific position.
     *
     * @param line the line to add
     * @return this instance
     */
    public ItemBuilder loreLine(String line) {
        if (this.meta == null) {
            this.meta = stack.getItemMeta();
        }

        boolean hasLore = meta.hasLore();
        List<String> lore = hasLore ? meta.getLore() : new ArrayList<>();
        lore.add(hasLore ? lore.size() : 0, line);

        this.lore(line);
        return this;
    }

    // This method is unsafe, can throw ArrayIndexOutOfBoundsException, and NullPointerException.
    public ItemBuilder loreLine(int index, String line) {
        if (this.meta == null) {
            this.meta = stack.getItemMeta();
        }

        List<String> lore = meta.getLore();
        lore.set(index, line);

        this.lore(lore.toArray(String[]::new));
        return this;
    }

    /**
     * Sets the lore of this item builder.
     *
     * @param lore the lore varargs to set
     * @return this instance
     */
    public ItemBuilder lore(String... lore) {
        if (this.meta == null) {
            this.meta = stack.getItemMeta();
        }

        meta.setLore(Arrays.asList(lore));
        return this;
    }

    /**
     * @see ItemBuilder#enchant(Enchantment, int, boolean)
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        return enchant(enchantment, level, true);
    }

    /**
     * Adds an enchantment to this item builder.
     *
     * @param enchantment the enchant to add
     * @param level       the level to add at
     * @param unsafe      if it should use unsafe calls
     * @return this instance
     */
    public ItemBuilder enchant(Enchantment enchantment, int level, boolean unsafe) {
        if (unsafe && level >= enchantment.getMaxLevel()) {
            stack.addUnsafeEnchantment(enchantment, level);
        } else {
            stack.addEnchantment(enchantment, level);
        }

        return this;
    }

    /**
     * Sets the data of this item builder.
     *
     * @param data the data value to set
     * @return the updated item builder
     */
    public ItemBuilder data(short data) {
        stack.setDurability(data);
        return this;
    }

    /**
     * Sets the owner of a skull of this item builder.
     *
     * @param player the owner
     * @return the updated item builder
     */
    public ItemBuilder owner(OfflinePlayer player) {
        owner(player.getName());
        return this;
    }

    public ItemBuilder owner(String name) {
        if (stack.getType() == Material.SKULL_ITEM) {
            ((SkullMeta) meta).setOwner(name);
        }
        return this;
    }

    public ItemBuilder ownerByURL(String url) {
        SkullMeta skullMeta = (SkullMeta) this.meta;
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        profileField.setAccessible(true);
        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        this.meta = skullMeta;
        return this;
    }
    /**
     * Set's the amount of the ItemStack
     *
     * @param amount the amount of the ItemStack
     * @return the updated item builder
     */
    public ItemBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    /**
     * Builds this into an {@link ItemStack}.
     *
     * @return the built {@link ItemStack}
     */
    public ItemStack build() {
        if (meta != null) {
            stack.setItemMeta(meta);
        }

        return stack;
    }
}
