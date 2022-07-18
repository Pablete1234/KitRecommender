package me.pablete1234.kit.recommender.util;

import me.pablete1234.kit.util.matrix.Matrix;
import me.pablete1234.kit.util.matrix.Row;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;

import static net.kyori.adventure.text.Component.text;

public class MatrixUtil {

    private static final int SMALL_WIDTH = 10; // Page
    private static final int BIG_WIDTH = 16; // Hover

    private static final ChatColor[][] PALETTE = new ChatColor[][]{
            new ChatColor[]{ChatColor.BLUE, ChatColor.DARK_AQUA}, // Non-zero
            new ChatColor[]{ChatColor.WHITE, ChatColor.GRAY},     // Black background (big, hover)
            new ChatColor[]{ChatColor.BLACK, ChatColor.DARK_GRAY} // White background (small)
    };

    public static Component toComponent(Matrix matrix, boolean hover) {
        int numWidth = hover ? BIG_WIDTH : SMALL_WIDTH;

        TextComponent.Builder result = text();
        for (int rIdx = 0; rIdx < matrix.size(); rIdx++) {
            Row row = matrix.getRow(rIdx);

            StringBuilder rowTxt = new StringBuilder();
            int offsetWidth = 0;

            for (int cIdx = 0; cIdx < row.size(); cIdx++) {
                if (cIdx > 0) rowTxt.append(ChatColor.RESET);
                if (cIdx == row.size() - 1) rowTxt.append(" |");


                Strings.SizedString str = Strings.asSizedString((int) Math.round(row.get(cIdx)), numWidth, offsetWidth);

                rowTxt.append(str.getLpad());

                ChatColor color = PALETTE[row.get(cIdx) != 0 ? 0 : hover ? 1 : 2][(rIdx + cIdx) % 2];

                rowTxt.append(color);
                if (rIdx == cIdx) rowTxt.append(ChatColor.ITALIC);
                rowTxt.append(str.getString());

                offsetWidth += numWidth - str.getWidth();
            }

            result.append(text(rowTxt.append("\n").toString()));
            if (rIdx == 8) {
                Strings.SizedString prev = Strings.fit(numWidth * 9);
                Strings.SizedString trail = Strings.fit(numWidth);
                result.append(text(prev.getString() + " |" + trail.getString() + "\n", Style.style(TextDecoration.STRIKETHROUGH)));
            }
        }
        return result.build();
    }


}
