package me.pablete1234.kit.recommender.util;

import net.md_5.bungee.api.ChatColor;

import static org.apache.commons.lang.StringUtils.repeat;

public class Strings {
    private static final String INFINITY = "\u221e";


    private static final int SPACE_WIDTH = 4;
    private static final int DEFAULT_WIDTH = 6;
    private static final int INFINITY_WIDTH = 8;
    private static final int BOLD_MODIFIER = 1;


    /**
     * Create a string representing the number, with a fixed size
     * @param num the number to represent
     * @param targetWidth the size in pixels intended for this number
     * @param offsetWidth how offset we are from target. Final width should be targetWidth - offsetWidth.
     * @return a left-padded string that should meet a size criteria
     */
    public static SizedString asSizedString(int num, int targetWidth, int offsetWidth) {
        int perfectWidth = targetWidth + offsetWidth;

        String result = Integer.toString(num);
        int width = result.length() * DEFAULT_WIDTH;

        // The number is just too big. Make it infinity and then work around.
        if (width > targetWidth + 2) {
            result = INFINITY;
            width = INFINITY_WIDTH;
        }

        // This is as short as we can get. If this is already too much, we can't do better.
        if (width >= perfectWidth) return new SizedString(result, width);

        SizedString pad = fit(perfectWidth - width);

        return new SizedString(pad.string, result, width + pad.width);
    }

    public static SizedString fit(int width) {
        if (width < 3) return new SizedString("", 0);

        // These are numbers which can't be exactly created, but have a very close alternative just 1-off.
        // It's better to use the 1-up than to fall 2 or 3 down.
        if (width == 3 || width == 7 || width == 11) width += 1;

        int totalChars = width / 4;

        int boldChars = Math.min(width % 4, totalChars);
        int normalChars = totalChars - boldChars;

        String result = "";
        if (normalChars > 0) result += repeat(" ", normalChars);
        if (boldChars > 0) result += ChatColor.BOLD + repeat(" ", boldChars) + ChatColor.RESET;

        return new SizedString(result, normalChars * SPACE_WIDTH + boldChars * (SPACE_WIDTH + BOLD_MODIFIER));
    }


    public static class SizedString {
        private final String lpad;
        private final String string;
        private final int width;

        public SizedString(String string, int width) {
            this("", string, width);
        }

        public SizedString(String lpad, String string, int width) {
            this.string = string;
            this.lpad = lpad;
            this.width = width;
        }

        public String getLpad() {
            return lpad;
        }

        public String getString() {
            return string;
        }

        public int getWidth() {
            return width;
        }
    }
}
