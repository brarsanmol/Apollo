package com.octavemc.util;

import com.octavemc.Apollo;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Credit for this class: https://gist.github.com/graywolf336/8153678
 */
@UtilityClass
public class InventorySerialisation {

    /**
     * Converts the player inventory to a String array of Base64 strings. First string is the content and second string is the armor.
     *
     * @param playerInventory to turn into an array of strings.
     * @return Array of strings: [ main content, armor content ]
     * @throws IllegalStateException if unable to return array
     */
    public static String[] playerInventoryToBase64(PlayerInventory playerInventory) throws IllegalStateException {
        //get the main content part, this doesn't return the armor
        String content = toBase64(playerInventory);
        String armor = itemStackArrayToBase64(playerInventory.getArmorContents());
        return new String[]{content, armor};
    }

    /**
     * A method to serialize an {@link ItemStack} array to Base64 String.
     * <p>Based off of {@link #toBase64(Inventory)}.</p>
     *
     * @param items to turn into a Base64 String.
     * @return Base64 string of the items.
     * @throws IllegalStateException if unable to save items
     */
    @SneakyThrows
    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        @Cleanup ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        @Cleanup BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        // Write the size of the inventory
        dataOutput.writeInt(items.length);

        // Save every element in the list
        for (ItemStack item : items) {
            dataOutput.writeObject(item);
        }

        // Serialize that array
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    @SneakyThrows
    public static String itemStackToBase64(ItemStack item) {
        @Cleanup ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        @Cleanup BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        // Write the size of the inventory
        dataOutput.writeInt(1);
        dataOutput.writeObject(item);

        // Serialize that array
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    @SneakyThrows
    public static ItemStack itemStackFromBase64(String data) {
        @Cleanup ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        @Cleanup BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

        return (ItemStack) dataInput.readObject();
    }

    /**
     * A method to serialize an inventory to Base64 string.
     * <p>Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.</p>
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     *
     * @param inventory to serialize
     * @return Base64 string of the provided inventory
     * @throws IllegalStateException if unable to save items
     */
    @SneakyThrows
    public static String toBase64(Inventory inventory) {
        return itemStackArrayToBase64(inventory.getContents());
    }

    /**
     * A method to get an {@link Inventory} from an encoded, Base64, string.
     * <p>Special thanks to Comphenix in the Bukkit forums or also known
     * as aadnk on GitHub.<p>
     * <a href="https://gist.github.com/aadnk/8138186">Original Source</a>
     *
     * @param data Base64 string of data containing an inventory.
     * @return Inventory created from the Base64 string.
     * @throws IOException if unable to decode the class type
     */
    public static Inventory fromBase64(String data) {
        ItemStack[] stacks = itemStackArrayFromBase64(data);
        Inventory inventory =  Apollo.getInstance().getServer().createInventory(null, data.length());
        inventory.setContents(stacks);
        return inventory;
    }

    /**
     * Gets an array of ItemStacks from Base64 string.
     * <p>Based off of {@link #fromBase64(String)}.</p>
     *
     * @param data Base64 string to convert to ItemStack array.
     * @return ItemStack array created from the Base64 string.
     * @throws IOException if unable to decode the class type
     */
    @SneakyThrows
    public static ItemStack[] itemStackArrayFromBase64(String data) {
        @Cleanup ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        @Cleanup BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack[] items = new ItemStack[dataInput.readInt()];

        // Read the serialized inventory
        for (int i = 0; i < items.length; i++) {
            items[i] = (ItemStack) dataInput.readObject();
        }
        return items;
    }
}
