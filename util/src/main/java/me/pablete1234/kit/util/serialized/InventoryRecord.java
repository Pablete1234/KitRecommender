package me.pablete1234.kit.util.serialized;

import blue.strategic.parquet.Dehydrator;
import blue.strategic.parquet.Hydrator;
import blue.strategic.parquet.HydratorSupplier;
import blue.strategic.parquet.ValueWriter;
import me.pablete1234.kit.util.InventoryImage;
import me.pablete1234.kit.util.StreamUtil;
import org.apache.parquet.schema.LogicalTypeAnnotation;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Types;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.PlayerInventory;

import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.parquet.schema.LogicalTypeAnnotation.TimeUnit.MILLIS;
import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.BOOLEAN;
import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.INT32;
import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.INT64;

public class InventoryRecord {

    private static final LogicalTypeAnnotation timestampType = LogicalTypeAnnotation.timestampType(true, MILLIS);

    public static final MessageType SCHEMA = new MessageType("inventory", StreamUtil.prepend(
            Types.required(INT64).as(timestampType).named("timestamp"),
            Types.required(BOOLEAN).named("closed"),
            IntStream.range(0, InventoryImage.PLAYER_SIZE).mapToObj(i -> Types.required(INT32).named("slot_" + i))
    ).collect(Collectors.toList()));

    private final long timestamp;
    private final boolean closed;
    private final InventoryImage inventory;

    public InventoryRecord(HumanEntity pl, boolean closed) {
        this(pl.getInventory(), closed);
    }

    public InventoryRecord(PlayerInventory inv, boolean closed) {
        this(Instant.now().toEpochMilli(), closed, new InventoryImage(inv));
    }

    public InventoryRecord(long timestamp, boolean closed, InventoryImage inv) {
        this.timestamp = timestamp;
        this.closed = closed;
        this.inventory = inv;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isClosedInventory() {
        return closed;
    }

    public boolean isAppliedKit() {
        return !closed;
    }

    public InventoryImage getInventory() {
        return inventory;
    }

    @Override
    public String toString() {
        return "InventoryRecord{" +
                "timestamp=" + timestamp +
                ", closed=" + closed +
                ", inv=" + inventory +
                '}';
    }

    public static class Builder {
        private long timestamp;
        private boolean closed;
        private final int[] contents;

        public Builder() {
            this.timestamp = Instant.now().toEpochMilli();
            this.closed = false;
            this.contents = new int[InventoryImage.PLAYER_SIZE];
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

        public InventoryRecord build() {
            return new InventoryRecord(timestamp, closed, new InventoryImage(contents));
        }
    }


    public static class Serializer implements Dehydrator<InventoryRecord> {
        public static final InventoryRecord.Serializer INSTANCE = new InventoryRecord.Serializer();

        @Override
        public void dehydrate(InventoryRecord rec, ValueWriter writer) {
            writer.write("timestamp", rec.timestamp);
            writer.write("closed", rec.closed);
            rec.inventory.write(writer, "slot_");
        }

    }

    public static class Deserializer implements Hydrator<InventoryRecord.Builder, InventoryRecord> {
        public static final InventoryRecord.Deserializer INSTANCE = new InventoryRecord.Deserializer();
        public static final HydratorSupplier<?, InventoryRecord> SUPPLIER_INSTANCE = HydratorSupplier.constantly(INSTANCE);

        @Override
        public InventoryRecord.Builder start() {
            return new InventoryRecord.Builder();
        }

        @Override
        public InventoryRecord.Builder add(InventoryRecord.Builder b, String s, Object o) {
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
        public InventoryRecord finish(InventoryRecord.Builder b) {
            return b.build();
        }

    }



}
