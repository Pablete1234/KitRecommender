package me.pablete1234.kit.recommender;

import me.pablete1234.kit.util.Categories;
import me.pablete1234.kit.util.category.Category;
import me.pablete1234.kit.util.matrix.Matrix;
import me.pablete1234.kit.util.matrix.Row;
import me.pablete1234.kit.util.model.KitPredictor;
import me.pablete1234.kit.util.model.NaiveBayesPredictor4;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import tc.oc.pgm.api.PGM;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public class PredictorManager implements Listener {

    private final Map<UUID, KitPredictor> predictors = new HashMap<>();
    private final ScheduledExecutorService asyncExecutor = PGM.get().getAsyncExecutor();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        UUID player = event.getUniqueId();
        predictors.put(player, loadPredictor(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        KitPredictor p = predictors.remove(event.getPlayer().getUniqueId());

        if (p instanceof NaiveBayesPredictor4) {
            UUID uuid = event.getPlayer().getUniqueId();
            Map<Category, Matrix> chances = ((NaiveBayesPredictor4) p).getChances();
            asyncExecutor.execute(() -> savePredictor(uuid, chances));
        } else {
            Bukkit.getLogger().log(Level.WARNING, "Cannot save predictor model, not naive bayes type");
        }
    }

    public @Nullable KitPredictor getPredictor(UUID player) {
        return predictors.get(player);
    }

    private KitPredictor loadPredictor(UUID player) {
        Path playerModel = new File(KitConfig.KIT_MODEL_FOLDER).toPath().resolve(player + ".km4");
        Map<Category, Matrix> data = new HashMap<>();

        if (Files.exists(playerModel)) {
            try (DataInputStream dis = new DataInputStream(Files.newInputStream(playerModel))) {
                while (dis.available() > 0) {
                    Category cat = Categories.of(Material.getMaterial(dis.readInt()));

                    Matrix matrix = new Matrix();
                    for (Row row : matrix) {
                        for (int i = 0; i < row.size(); i++) {
                            row.set(i, dis.readDouble());
                        }
                    }
                    data.put(cat, matrix);
                }
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to read model for player " + player, e);
            }
        }

        return new NaiveBayesPredictor4(data);
    }

    private void savePredictor(UUID player, Map<Category, Matrix> data) {
        Path modelFolder = new File(KitConfig.KIT_MODEL_FOLDER).toPath();
        try {
            Files.createDirectories(modelFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Path playerModel = modelFolder.resolve(player + ".km4");

        try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(playerModel, StandardOpenOption.CREATE))) {
            for (Map.Entry<Category, Matrix> entry : data.entrySet()) {
                dos.writeInt(entry.getKey().getAll().iterator().next().getId());
                for (Row row : entry.getValue()) for (double cell : row) dos.writeDouble(cell);
            }
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to write model for player " + player, e);
        }
    }

}
