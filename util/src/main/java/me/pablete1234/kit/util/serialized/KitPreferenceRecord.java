package me.pablete1234.kit.util.serialized;

import blue.strategic.parquet.Dehydrator;
import blue.strategic.parquet.ValueWriter;
import me.pablete1234.kit.util.InventoryImage;
import me.pablete1234.kit.util.StreamUtil;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;
import org.apache.parquet.schema.Types;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.BOOLEAN;
import static org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.INT32;

public class KitPreferenceRecord {

    public static final MessageType SCHEMA = new MessageType("player_record", StreamUtil.<Type>concat(
                    Types.required(INT32).named("timeframe_seconds"),
                    Types.required(BOOLEAN).named("edited_inventory"),
                    IntStream.range(0, InventoryImage.PLAYER_SIZE)
                            .mapToObj(i -> Types.required(INT32).named("kit_" + i)),
                    IntStream.range(0, InventoryImage.PLAYER_SIZE)
                            .mapToObj(i -> Types.required(INT32).named("sorted_" + i)))
            .collect(Collectors.toList()));

    private final int timeframe;
    private final boolean editedInventory;
    private final InventoryImage kit;
    private final InventoryImage sorted;

    public KitPreferenceRecord(int timeframe, boolean editedInventory, InventoryImage kit, InventoryImage sorted) {
        this.timeframe = timeframe;
        this.editedInventory = editedInventory;
        this.kit = kit;
        this.sorted = sorted;
    }

    public static class Serializer implements Dehydrator<KitPreferenceRecord> {
        public static final KitPreferenceRecord.Serializer INSTANCE = new KitPreferenceRecord.Serializer();

        @Override
        public void dehydrate(KitPreferenceRecord inv, ValueWriter writer) {
            writer.write("timeframe_seconds", inv.timeframe);
            writer.write("edited_inventory", inv.editedInventory);
            inv.kit.write(writer, "kit_");
            inv.sorted.write(writer, "sorted_");
        }
    }


}
