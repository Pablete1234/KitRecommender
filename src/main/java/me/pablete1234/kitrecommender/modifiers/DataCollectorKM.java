package me.pablete1234.kitrecommender.modifiers;

import blue.strategic.parquet.ParquetWriter;
import me.pablete1234.kitrecommender.KitConfig;
import me.pablete1234.kitrecommender.itf.KitModifier;
import me.pablete1234.kitrecommender.utils.InventoryImage;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import tc.oc.pgm.api.PGM;
import tc.oc.pgm.kits.ApplyItemKitEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class DataCollectorKM implements KitModifier {

    private static final DateTimeFormatter FILENAME_DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss");

    private final ScheduledExecutorService syncExecutor = PGM.get().getExecutor();
    private final ScheduledExecutorService asyncExecutor = PGM.get().getAsyncExecutor();

    private final UUID player;
    private final KitModifier downstream;
    private final ParquetWriter<InventoryImage> writer;

    private final AtomicBoolean applyingKit = new AtomicBoolean(false);

    public DataCollectorKM(UUID player, KitModifier downstream) throws IOException {
        this.player = player;
        this.downstream = downstream;
        Path playerFolder = new File(KitConfig.KIT_DATA_FOLDER).toPath().resolve(player.toString());
        Files.createDirectories(playerFolder);
        Path file = playerFolder.resolve(FILENAME_DATE.format(LocalDateTime.now()) + ".parquet");
        this.writer = ParquetWriter.writeFile(InventoryImage.SCHEMA, file.toFile(), InventoryImage.Serializer.INSTANCE);
    }

    @Override
    public void adjustKit(ApplyItemKitEvent event) {
        this.downstream.adjustKit(event);
        // Make it so multiple item kits applying all at once do not cause multiple writes,
        // since they are deferred to the end of the tick
        if (this.applyingKit.compareAndSet(false, true)) {
            this.syncExecutor.execute(() -> {
                this.write(new InventoryImage(event.getPlayer().getBukkit(), false));
                this.applyingKit.set(false);
            });
        }
    }

    @Override
    public void learnPreferences(InventoryCloseEvent event) {
        this.write(new InventoryImage(event.getPlayer(), true));
        this.downstream.learnPreferences(event);
    }

    @Override
    public void cleanKits(UUID player) {
        if (!this.player.equals(player))
            throw new UnsupportedOperationException("Tried to cleanup a kit modifier for a different player.");
        this.downstream.cleanKits(player);
    }

    @Override
    public void cleanup(UUID player) {
        if (!this.player.equals(player))
            throw new UnsupportedOperationException("Tried to cleanup a kit modifier for a different player.");

        this.close();
        this.downstream.cleanup(player);
    }

    @Override
    public void cleanup() {
        this.close();
        this.downstream.cleanup();
    }

    private void write(InventoryImage image) {
        asyncExecutor.execute(() -> {
            try {
                writer.write(image);
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to write inventory image for player " + player, e);
            }
        });
    }

    private void close() {
        asyncExecutor.execute(() -> {
            try {
                writer.close();
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to close parquet writer for player " + player, e);
            }
        });
    }
}
