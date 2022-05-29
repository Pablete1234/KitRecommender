package me.pablete1234.kit.recommender;

import me.pablete1234.kit.recommender.itf.KitModifier;
import me.pablete1234.kit.recommender.modifiers.DataCollectorKM;
import me.pablete1234.kit.recommender.modifiers.GlobalToPlayerKM;
import me.pablete1234.kit.recommender.modifiers.PlayerKitModel;
import me.pablete1234.kit.recommender.modifiers.PlayerToKitKM;
import me.pablete1234.kit.util.model.KitPredictor;
import me.pablete1234.kit.util.model.NaiveBayesPredictor3;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KitRecommender extends JavaPlugin {

    private PredictorManager predictors;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        KitConfig.setConfig(getConfig());

        Function<UUID, KitModifier> playerToKit = pl -> new PlayerToKitKM(pl, predictors.getPredictor(pl), PlayerKitModel::new);
        Function<UUID, KitModifier> dataCollector = pl -> {
            KitModifier downstream = playerToKit.apply(pl);
            try {
                return new DataCollectorKM(pl, downstream);
            } catch (IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to create DataCollector for " + pl, e);
                return downstream;
            }
        };
        GlobalToPlayerKM globalToPlayer = new GlobalToPlayerKM(KitConfig.COLLECT_DATA ? dataCollector : playerToKit);

        KitListener listener = new KitListener(globalToPlayer);
        getServer().getPluginManager().registerEvents(listener, this);

        predictors = new PredictorManager();
        getServer().getPluginManager().registerEvents(predictors, this);

        getCommand("kitdata").setExecutor(this);
        getLogger().info("KitRecommender has been enabled!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        getLogger().info("KitRecommender has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equals("kitdata")) return false;

        UUID target = args.length == 1 ? Bukkit.getPlayer(args[0], sender).getUniqueId() :
                sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "When running from console you must specify a target player");
            return true;
        }

        KitPredictor p = predictors.getPredictor(target);
        if (!(p instanceof NaiveBayesPredictor3)) {
            sender.sendMessage(ChatColor.RED + "Currently used model couldn't be displayed!");
            return true;
        }

        NaiveBayesPredictor3 nbp = (NaiveBayesPredictor3) p;

        if (nbp.getChances().isEmpty()) {
            sender.sendMessage("No preferences have yet been recorded");
        }

        nbp.getChances().forEach((c, m) -> {
            sender.sendMessage("Category: " + c);
            sender.sendMessage(IntStream.range(0, m.size())
                    .mapToObj(m::getRow)
                    .map(row -> IntStream.range(0, row.size())
                            .mapToDouble(row::get)
                            .mapToObj(val -> String.format("%4.1f", val))
                            .collect(Collectors.joining("  ")))
                    .collect(Collectors.joining("\n")));
        });
        return true;
    }
}
