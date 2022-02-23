package me.pablete1234.kitrecommender.modifiers;

import blue.strategic.parquet.ParquetWriter;
import me.pablete1234.kitrecommender.itf.KitModifier;
import me.pablete1234.kitrecommender.utils.InventoryImage;
import org.apache.parquet.schema.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import tc.oc.pgm.kits.ApplyItemKitEvent;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class DataCollectorKM implements KitModifier {

    private static final MessageType schema = null;

    private final UUID player;
    private final KitModifier downstream;
    private final ParquetWriter<InventoryImage> writer;

    public DataCollectorKM(UUID player, KitModifier downstream) throws IOException {
        this.player = player;
        this.downstream = downstream;
        this.writer = ParquetWriter.writeFile(schema,
                new File(player.toString() + ".parquet"),
                InventoryImage.Serializer.INSTANCE);
    }

    @Override
    public void adjustKit(ApplyItemKitEvent event) {
        this.downstream.adjustKit(event);
    }

    @Override
    public void learnPreferences(InventoryCloseEvent event) {
        InventoryImage img = InventoryImage.from(event.getPlayer().getInventory());
        try {
            writer.write(img);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.downstream.learnPreferences(event);
    }

    @Override
    public void cleanKits(UUID player) {
        this.downstream.cleanKits(player);
    }

    @Override
    public void cleanup(UUID player) {
        this.downstream.cleanup(player);
        try {
            writer.close();
        } catch (IOException e) {
            Bukkit.getLogger().log(
                    Level.WARNING,
                    "Failed to close parquet writer for player " + player.toString(),
                    e);
        }
    }

    @Override
    public void cleanup() {
        this.downstream.cleanup();
    }
}
