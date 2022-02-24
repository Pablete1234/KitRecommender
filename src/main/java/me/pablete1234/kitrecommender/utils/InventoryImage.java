package me.pablete1234.kitrecommender.utils;

import blue.strategic.parquet.Dehydrator;
import blue.strategic.parquet.ValueWriter;
import me.pablete1234.kitrecommender.utils.category.Category;
import org.apache.parquet.schema.*;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.parquet.schema.LogicalTypeAnnotation.TimeUnit.MILLIS;
import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.BOOLEAN;
import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.INT32;
import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.INT64;

public class InventoryImage {
    public static final int PLAYER_SIZE = 36;

    private static final LogicalTypeAnnotation timestampType = LogicalTypeAnnotation.timestampType(true, MILLIS);

    public static final MessageType SCHEMA = new MessageType("inventory", StreamUtil.append(
            IntStream.range(0, PLAYER_SIZE)
                    .mapToObj(i -> Types.required(INT32).named("slot_" + i)),
            Types.required(INT64).as(timestampType).named("timestamp"),
            Types.required(BOOLEAN).named("closed")
    ).collect(Collectors.toList()));

    private final int[] contents;
    private final long timestamp;
    private final boolean closed;

    public InventoryImage(HumanEntity pl, boolean closed) {
        this(pl.getInventory(), closed);
    }

    public InventoryImage(PlayerInventory inv, boolean closed) {
        this.timestamp = Instant.now().toEpochMilli();
        this.contents = new int[PLAYER_SIZE];
        for (int i = 0; i < PLAYER_SIZE; i++)
            this.contents[i] = serialize(inv.getItem(i));

        this.closed = closed;
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

    @SuppressWarnings({"deprecation", "unused"})
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
        public void dehydrate(InventoryImage inv, ValueWriter valueWriter) {
            for (int i = 0; i < PLAYER_SIZE; i++)
                valueWriter.write("slot_" + i, inv.contents[i]);
            valueWriter.write("timestamp", inv.timestamp);
            valueWriter.write("closed", inv.closed);
        }
    }

}
