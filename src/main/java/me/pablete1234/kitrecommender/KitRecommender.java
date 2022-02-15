package me.pablete1234.kitrecommender;

import me.pablete1234.kitrecommender.engine.PlayerKitModel;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class KitRecommender extends JavaPlugin {

    @Override
    public void onEnable() {
        // For now, we use the hard-coded player kit model
        // In the future the factory would involve calling to create a custom model for the requested player
        KitListener listener = new KitListener(PlayerKitModel::new);
        getServer().getPluginManager().registerEvents(listener, this);

        getLogger().info("KitRecommender has been enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        getLogger().info("KitRecommender has been disabled!");
    }



}
