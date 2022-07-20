package me.pablete1234.kit.recommender;

import me.pablete1234.kit.recommender.itf.KitModifier;
import me.pablete1234.kit.recommender.modifiers.DataCollectorKM;
import me.pablete1234.kit.recommender.modifiers.GlobalToPlayerKM;
import me.pablete1234.kit.recommender.modifiers.PlayerToKitKM;
import me.pablete1234.kit.recommender.util.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

public class KitRecommender extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        KitConfig.setConfig(getConfig());

        PredictorManager predictors = new PredictorManager();

        Function<UUID, KitModifier> playerToKit = pl -> new PlayerToKitKM(pl, predictors.getPredictor(pl));
        GlobalToPlayerKM globalToPlayer = new GlobalToPlayerKM(KitConfig.COLLECT_DATA ? pl -> {
            KitModifier downstream = playerToKit.apply(pl);
            try {
                return new DataCollectorKM(pl, downstream);
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to create DataCollector for " + pl, e);
                return downstream;
            }
        } : playerToKit);

        KitListener listener = new KitListener(globalToPlayer);
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getPluginManager().registerEvents(predictors, this);

        CommandHandler commandHandler = new CommandHandler(predictors);
        getCommand("kitdata").setExecutor(commandHandler);


        getLogger().info("KitRecommender has been enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        getLogger().info("KitRecommender has been disabled!");
    }

}
