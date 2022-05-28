package me.pablete1234.kit.util;

import blue.strategic.parquet.ValueWriter;
import me.pablete1234.kit.util.category.Category;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class InventoryImage {
    public static final int PLAYER_SIZE = 36;

    public static final int MATERIAL_MASK = 0xFFFF << 16;
    public static final int AMOUNT_MASK = 0xFF << 8;
    public static final int DATA_MASK = 0xFF;

    private final int[] contents;

    public InventoryImage(HumanEntity pl) {
        this(pl.getInventory());
    }

    public InventoryImage(PlayerInventory inv) {
        this.contents = new int[PLAYER_SIZE];
        for (int i = 0; i < PLAYER_SIZE; i++)
            this.contents[i] = serialize(inv.getItem(i));
    }

    public InventoryImage(int[] contents) {
        if (contents.length != PLAYER_SIZE)
            throw new IllegalArgumentException("InventoryImage must be of size " + PLAYER_SIZE);
        this.contents = contents;
    }

    public int getItem(int slot) {
        return contents[slot];
    }

    public void copyInto(int[] contents) {
        System.arraycopy(this.contents, 0, contents, 0, PLAYER_SIZE);
    }

    public boolean maybeContains(int item) {
        int material = item & MATERIAL_MASK;
        for (int content : contents)
            if ((content & MATERIAL_MASK) == material) return true;
        return false;
    }

    public void write(ValueWriter writer, String prefix) {
        for (int i = 0; i < PLAYER_SIZE; i++)
            writer.write(prefix + i, contents[i]);
    }

    public static int serialize(ItemStack is) {
        if (is == null) return 0;
        short material = (short) is.getTypeId();
        byte  amount   = (byte)  is.getAmount();

        Category cat = Categories.of(is.getType());
        byte data = cat.getData(is);

        return material << 16 | amount << 8 | data;
    }

    public static ItemStack deserialize(int serialized) {
        short material = (short) (serialized >> 16 & 0xffff);
        byte amount    = (byte)  (serialized >> 8 & 0xff);
        byte data      = (byte)  (serialized & 0xff);

        Material type = Material.getMaterial(material);
        Category cat = Categories.of(type);

        ItemStack is = new ItemStack(material, amount);
        cat.putData(is, data);
        return is;
    }

    public static Material getMaterial(int serialized) {
        short material = (short) (serialized >> 16 & 0xffff);
        return Material.getMaterial(material);
    }

    @Override
    public String toString() {
        return "InventoryImage{" +
                "contents=" + Arrays.stream(contents)
                        .mapToObj(InventoryImage::deserialize)
                        .collect(Collectors.toList()) +
                '}';
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

}
