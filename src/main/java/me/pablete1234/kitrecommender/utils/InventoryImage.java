package me.pablete1234.kitrecommender.utils;

import me.pablete1234.kitrecommender.utils.category.Category;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import java.util.Arrays;

public class InventoryImage {
    public static final int PLAYER_SIZE = 36;

    private final int[] contents;

    public InventoryImage(int[] contents) {
        if (contents.length != PLAYER_SIZE)
            throw new IllegalArgumentException("InventoryImage must have exactly " + PLAYER_SIZE + " items");
        this.contents = contents;
    }


    public static InventoryImage from(PlayerInventory inventory) {
        int[] contents = new int[PLAYER_SIZE];
        for (int i = 0; i < PLAYER_SIZE; i++)
            contents[i] = serialize(inventory.getItem(i));
        return new InventoryImage(contents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryImage)) return false;

        InventoryImage that = (InventoryImage) o;
        return Arrays.equals(contents, that.contents);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(contents);
    }

    @SuppressWarnings("deprecation")
    public static int serialize(ItemStack is) {
        short material = (short) is.getTypeId();
        byte  amount   = (byte)  is.getAmount();
        byte  data     = 0;

        Category cat = Categories.of(is.getType());
        if (cat != null) data = cat.getData(is);
        else if (is.getData().getClass() != MaterialData.class) data = is.getData().getData();

        return material << 16 | amount << 8 | data;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack deserialize(int serialized) {
        short material = (short) (serialized >> 16 & 0xffff);
        byte amount    = (byte)  (serialized >> 8 & 0xff);
        byte data      = (byte)  (serialized & 0xff);

        Material type = Material.getMaterial(material);
        Category cat = Categories.of(type);

        if (cat != null) {
            ItemStack is = new ItemStack(material, amount);
            cat.putData(is, data);
            return is;
        } else {
            return new ItemStack(material, amount, (short) 0, data);
        }
    }

}
