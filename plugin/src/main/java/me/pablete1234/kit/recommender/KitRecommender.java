package me.pablete1234.kit.recommender;

import me.pablete1234.kit.recommender.itf.KitModifier;
import me.pablete1234.kit.recommender.modifiers.DataCollectorKM;
import me.pablete1234.kit.recommender.modifiers.GlobalToPlayerKM;
import me.pablete1234.kit.recommender.modifiers.PlayerKitModel;
import me.pablete1234.kit.recommender.modifiers.PlayerToKitKM;
import me.pablete1234.kit.util.category.Category;
import me.pablete1234.kit.util.matrix.Matrix;
import me.pablete1234.kit.util.matrix.Row;
import me.pablete1234.kit.util.model.KitPredictor;
import me.pablete1234.kit.util.model.NaiveBayesPredictor4;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import tc.oc.pgm.util.Audience;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.HoverEvent.showText;

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

        Player target = args.length == 1 ? Bukkit.getPlayer(args[0], sender) :
                sender instanceof Player ? ((Player) sender) : null;

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "When running from console you must specify a target player");
            return true;
        }

        KitPredictor p = predictors.getPredictor(target.getUniqueId());
        if (!(p instanceof NaiveBayesPredictor4)) {
            sender.sendMessage(ChatColor.RED + "Currently used model couldn't be displayed!");
            return true;
        }

        NaiveBayesPredictor4 nbp = (NaiveBayesPredictor4) p;

        if (nbp.getChances().isEmpty()) {
            sender.sendMessage("No preferences have yet been recorded");
        }

        TextComponent.Builder overview = text();
        overview.append(text(target.getName(sender) + "'s kits:", NamedTextColor.GOLD));
        overview.append(newline());

        List<Component> pages = nbp.getChances()
                .entrySet()
                .stream()
                .sorted(Comparator.comparingDouble(k -> -IntStream.range(0, k.getValue().size())
                        .mapToObj(k.getValue()::getRow)
                        .flatMapToDouble(r -> IntStream.range(0, r.size()).mapToDouble(r::get))
                        .sum()))
                .map(e -> {
                    String cat = e.getKey().toString().toLowerCase(Locale.ROOT).replace("_", " ");
                    Matrix m = e.getValue();

                    overview.append(text()
                            .append(text(cat, NamedTextColor.DARK_AQUA))
                            .hoverEvent(showText(text()
                                    .append(text(cat + " matrix:\n", NamedTextColor.DARK_PURPLE))
                                    .append(getMatrixComponent(m, true))
                                    .build()))).append(newline());

                    return text()
                            .append(text(cat, NamedTextColor.DARK_PURPLE))
                            .append(newline())
                            .append(newline())
                            .append(getMatrixComponent(m, false)).build();
        }).collect(Collectors.toList());

        Audience.get(sender).openBook(Book.builder()
                .author(text("Kit Recommender"))
                .addPage(overview.build())
                .pages(pages)
                .build()
        );
        return true;
    }

    private Component getMatrixComponent(Matrix matrix, boolean white) {
        TextComponent.Builder result = text();
        for (int rIdx = 0; rIdx < matrix.size(); rIdx++) {
            Row row = matrix.getRow(rIdx);

            StringBuilder rowTxt = new StringBuilder();

            for (int cIdx = 0; cIdx < row.size(); cIdx++) {
                if (cIdx > 0) rowTxt.append(ChatColor.RESET).append(" ");
                if (cIdx == row.size() - 1) rowTxt.append("| ");

                ChatColor color = row.get(cIdx) != 0 ? ChatColor.BLUE :
                        ((rIdx + cIdx) % 2 == 0 ?
                                white ? ChatColor.WHITE : ChatColor.BLACK :
                                white ? ChatColor.GRAY : ChatColor.DARK_GRAY);

                rowTxt.append(color);
                if (rIdx == cIdx) rowTxt.append(ChatColor.ITALIC);
                rowTxt.append(String.format(white ? "%2.0f" : "%1.0f", row.get(cIdx)));
            }

            result.append(text(rowTxt.append("\n").toString()));
            if (rIdx == 8) {
                result.append(text("  ", Style.style(TextDecoration.STRIKETHROUGH, TextDecoration.BOLD)));
                result.append(text((white ? "         " : "") +
                        "                    |   \n", Style.style(TextDecoration.STRIKETHROUGH)));
            }
        }
        return result.build();
    }
}
