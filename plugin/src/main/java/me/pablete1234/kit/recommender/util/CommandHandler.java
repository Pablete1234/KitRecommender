package me.pablete1234.kit.recommender.util;

import me.pablete1234.kit.recommender.PredictorManager;
import me.pablete1234.kit.util.category.Category;
import me.pablete1234.kit.util.matrix.Matrix;
import me.pablete1234.kit.util.model.KitPredictor;
import me.pablete1234.kit.util.model.NaiveBayesPredictor4;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import tc.oc.pgm.util.Audience;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.HoverEvent.showText;

public class CommandHandler implements CommandExecutor {

    private final PredictorManager predictors;

    private final Comparator<Map.Entry<Category, Matrix>> COMPARATOR =
            Map.Entry.<Category, Matrix>comparingByValue().reversed();

    private static final String KITDATA_COMMAND = "kitdata";
    private static final String KITDATA_ALL_PERMISSION = "kitrecommender.kitdata.all";

    public CommandHandler(PredictorManager predictors) {
        this.predictors = predictors;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (KITDATA_COMMAND.equals(label)) {
            String res = handleKitData(sender, command, args);
            if (res != null) sender.sendMessage(ChatColor.RED + res);
            return true;
        }
        return false;
    }

    private String handleKitData(CommandSender sender, Command command, String[] args) throws CommandException {
        if (args.length != 0 && args.length != 1) return "Invalid usage: " + command.getUsage();
        if (!sender.hasPermission(KITDATA_ALL_PERMISSION) && args.length == 1)
            return "You do not have permission";

        if (!(sender instanceof Player)) return "This command cannot be used from the console";

        Player target = args.length == 1 && sender.hasPermission(KITDATA_ALL_PERMISSION) ?
                Bukkit.getPlayer(args[0], sender) : (Player) sender;

        if (target == null) return "Player not found";

        KitPredictor p = predictors.getPredictor(target.getUniqueId());
        if (!(p instanceof NaiveBayesPredictor4)) return "Currently used model couldn't be displayed!";

        NaiveBayesPredictor4 nbp = (NaiveBayesPredictor4) p;

        if (nbp.getChances().isEmpty()) return "No preferences have yet been recorded";

        TextComponent.Builder overview = text();
        overview.append(text(target.getName(target) + "'s kits:", NamedTextColor.GOLD));
        overview.append(newline());

        Map.Entry<List<Component>, List<Component>> categories = nbp.getChances().entrySet()
                .stream()
                .sorted(COMPARATOR)
                .map(e -> renderCategory(e.getKey(), e.getValue()))
                .collect(() -> new AbstractMap.SimpleEntry<>(new ArrayList<>(), new ArrayList<>()),
                        (acc, entry) -> {
                            acc.getKey().add(entry.getKey());
                            acc.getValue().add(entry.getValue());
                        },
                        (e1, e2) -> {
                            e1.getKey().addAll(e2.getKey());
                            e1.getValue().addAll(e2.getValue());
                        });

        // Show at most 15 categories on main page, or else book becomes invalid
        overview.append(categories.getKey().subList(0, Math.min(categories.getKey().size(), 15)));

        Audience.get(sender).openBook(Book.builder()
                .author(text("Kit Recommender"))
                .addPage(overview.build())
                .pages(categories.getValue())
                .build());
        return null;
    }

    private Map.Entry<TextComponent, TextComponent> renderCategory(Category category, Matrix m) {
        String cat = category.toHumanString();

        TextComponent indexComponent = text(cat, NamedTextColor.DARK_AQUA)
                .hoverEvent(showText(text()
                        .append(text(cat + " matrix:\n", NamedTextColor.DARK_PURPLE))
                        .append(MatrixUtil.toComponent(m, true))
                        .build()))
                .append(newline());

        TextComponent pageComponent = text(cat, NamedTextColor.DARK_PURPLE)
                .append(newline())
                .append(newline())
                .append(MatrixUtil.toComponent(m, false).color(NamedTextColor.BLACK));

        return new AbstractMap.SimpleEntry<>(indexComponent, pageComponent);
    }


}
