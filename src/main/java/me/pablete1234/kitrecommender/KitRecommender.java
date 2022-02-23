package me.pablete1234.kitrecommender;

import me.pablete1234.kitrecommender.itf.KitModifier;
import me.pablete1234.kitrecommender.modifiers.DataCollectorKM;
import me.pablete1234.kitrecommender.modifiers.GlobalToPlayerKM;
import me.pablete1234.kitrecommender.modifiers.PlayerKitModel;
import me.pablete1234.kitrecommender.modifiers.PlayerToKitKM;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

public class KitRecommender extends JavaPlugin {

    private static final boolean STORE_KITS = true;

    @Override
    public void onEnable() {
        Function<UUID, KitModifier> playerToKit = pl -> new PlayerToKitKM(pl, PlayerKitModel::new);
        Function<UUID, KitModifier> dataCollector = pl -> {
            KitModifier downstream = playerToKit.apply(pl);
            try {
                return new DataCollectorKM(pl, downstream);
            } catch (IOException e) {
                Bukkit.getLogger().log(
                        Level.WARNING,
                        "Failed to create DataCollector for " + pl.toString(),
                        e);
                return downstream;
            }
        };
        GlobalToPlayerKM globalToPlayer = new GlobalToPlayerKM(STORE_KITS ? dataCollector : playerToKit);

        KitListener listener = new KitListener(globalToPlayer);
        getServer().getPluginManager().registerEvents(listener, this);

        getLogger().info("KitRecommender has been enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        getLogger().info("KitRecommender has been disabled!");
    }



}
