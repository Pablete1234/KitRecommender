package me.pablete1234.kit.util;

import blue.strategic.parquet.Dehydrator;
import blue.strategic.parquet.Hydrator;
import blue.strategic.parquet.HydratorSupplier;
import blue.strategic.parquet.ValueWriter;
import me.pablete1234.kit.util.category.Category;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Types;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
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

    public static final int MATERIAL_MASK = 0xFFFF << 16;
    public static final int AMOUNT_MASK = 0xFF << 8;
    public static final int DATA_MASK = 0xFF;

    private static final LogicalTypeAnnotation timestampType = LogicalTypeAnnotation.timestampType(true, MILLIS);

    public static final MessageType SCHEMA = new MessageType("inventory", StreamUtil.prepend(
            Types.required(INT64).as(timestampType).named("timestamp"),
            Types.required(BOOLEAN).named("closed"),
            IntStream.range(0, PLAYER_SIZE).mapToObj(i -> Types.required(INT32).named("slot_" + i))
    ).collect(Collectors.toList()));

    private final long timestamp;
    private final boolean closed;
    private final int[] contents;

    public InventoryImage(HumanEntity pl, boolean closed) {
        this(pl.getInventory(), closed);
    }

    public InventoryImage(PlayerInventory inv, boolean closed) {
        this.timestamp = Instant.now().toEpochMilli();
        this.closed = closed;
        this.contents = new int[PLAYER_SIZE];
        for (int i = 0; i < PLAYER_SIZE; i++)
            this.contents[i] = serialize(inv.getItem(i));
    }

    public InventoryImage(long timestamp, boolean closed, int[] contents) {
        this.timestamp = timestamp;
        this.closed = closed;
        this.contents = contents;
    }

    public int getItem(int slot) {
        return contents[slot];
    }

    public boolean maybeContains(int item) {
        int material = item & MATERIAL_MASK;
        for (int content : contents)
            if ((content & MATERIAL_MASK) == material) return true;
        return false;
    }

    @SuppressWarnings("deprecation")
    public static int serialize(ItemStack is) {
        if (is == null) return 0;
        short material = (short) is.getTypeId();
        byte  amount   = (byte)  is.getAmount();
        byte  data     = 0;

        Category cat = Categories.of(is.getType());
        if (cat != null) data = cat.getData(is);
        else if (is.getData().getClass() != MaterialData.class) data = is.getData().getData();

        return material << 16 | amount << 8 | data;
    }

    @SuppressWarnings({"deprecation"})
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

    @SuppressWarnings({"deprecation"})
    public static Material getMaterial(int serialized) {
        short material = (short) (serialized >> 16 & 0xffff);
        return Material.getMaterial(material);
    }

    @Override
    public String toString() {
        return "InventoryImage{" +
                "timestamp=" + timestamp +
                ", closed=" + closed +
                ", contents=" + Arrays.stream(contents)
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

    public static class Builder {
        private long timestamp;
        private boolean closed;
        private final int[] contents;

        public Builder() {
            this.timestamp = Instant.now().toEpochMilli();
            this.closed = false;
            this.contents = new int[PLAYER_SIZE];
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder closed(boolean closed) {
            this.closed = closed;
            return this;
        }

        public Builder item(int slot, int item) {
            this.contents[slot] = item;
            return this;
        }

        public InventoryImage build() {
            return new InventoryImage(timestamp, closed, contents);
        }
    }

    public static class Serializer implements Dehydrator<InventoryImage> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void dehydrate(InventoryImage inv, ValueWriter writer) {
            writer.write("timestamp", inv.timestamp);
            writer.write("closed", inv.closed);
            for (int i = 0; i < PLAYER_SIZE; i++)
                writer.write("slot_" + i, inv.contents[i]);
        }
    }

    public static class Deserializer implements Hydrator<InventoryImage.Builder, InventoryImage> {
        public static final Deserializer INSTANCE = new Deserializer();
        public static final HydratorSupplier<?, InventoryImage> SUPPLIER_INSTANCE = HydratorSupplier.constantly(INSTANCE);

        @Override
        public InventoryImage.Builder start() {
            return new InventoryImage.Builder();
        }

        @Override
        public InventoryImage.Builder add(InventoryImage.Builder b, String s, Object o) {
            switch (s) {
                case "timestamp": return b.timestamp((long) o);
                case "closed": return b.closed((boolean) o);
                default:
                    if (!s.startsWith("slot_")) return b;
                    int slot = Integer.parseInt(s.substring(5));
                    return b.item(slot, (int) o);
            }
        }

        @Override
        public InventoryImage finish(InventoryImage.Builder b) {
            return b.build();
        }

    }

}
