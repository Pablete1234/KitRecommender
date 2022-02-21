package me.pablete1234.kitrecommender;

import me.pablete1234.kitrecommender.itf.KitModifier;
import me.pablete1234.kitrecommender.modifiers.DataCollectorKM;
import me.pablete1234.kitrecommender.modifiers.GlobalToPlayerKM;
import me.pablete1234.kitrecommender.modifiers.PlayerKitModel;
import me.pablete1234.kitrecommender.modifiers.PlayerToKitKM;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.function.Function;

public class KitRecommender extends JavaPlugin {

    private static final boolean STORE_KITS = true;

    @Override
    public void onEnable() {
        Function<UUID, KitModifier> playerToKit = pl -> new PlayerToKitKM(pl, PlayerKitModel::new);
        Function<UUID, KitModifier> dataCollector = pl -> new DataCollectorKM(pl, playerToKit.apply(pl));
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
