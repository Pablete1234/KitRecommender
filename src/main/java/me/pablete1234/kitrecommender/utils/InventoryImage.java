package me.pablete1234.kitrecommender.utils;

import blue.strategic.parquet.Dehydrator;
import blue.strategic.parquet.ValueWriter;
import me.pablete1234.kitrecommender.utils.category.Category;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import java.util.Arrays;

public class InventoryImage {
    public static final int PLAYER_SIZE = 36;

    private final long timestamp;
    private final int[] contents;

    public InventoryImage(long timestamp, int[] contents) {
        if (contents.length != PLAYER_SIZE)
            throw new IllegalArgumentException("InventoryImage must have exactly " + PLAYER_SIZE + " items");
        this.timestamp = timestamp;
        this.contents = contents;
    }

    public static InventoryImage from(PlayerInventory inventory) {
        int[] contents = new int[PLAYER_SIZE];
        for (int i = 0; i < PLAYER_SIZE; i++)
            contents[i] = serialize(inventory.getItem(i));
        return new InventoryImage(System.currentTimeMillis(), contents);
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

    @Override
    public boolean equals(Object o) {
        return this == o ||
                (o instanceof InventoryImage && Arrays.equals(contents, ((InventoryImage) o).contents));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(contents);
    }

    public static class Serializer implements Dehydrator<InventoryImage> {
        public static final Serializer INSTANCE = new Serializer();
        @Override
        public void dehydrate(InventoryImage inventoryImage, ValueWriter valueWriter) {
            valueWriter.write("timestamp", inventoryImage.timestamp);
            for (int i = 0; i < PLAYER_SIZE; i++)
                valueWriter.write("slot_" + i, inventoryImage.contents[i]);
        }
    }

}
