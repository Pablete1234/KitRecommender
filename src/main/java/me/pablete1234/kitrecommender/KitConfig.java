package me.pablete1234.kitrecommender;

import org.bukkit.configuration.file.FileConfiguration;

public class KitConfig {
    public static boolean COLLECT_DATA = true;
    public static String KIT_DATA_FOLDER = "kit_data";

    public static void setConfig(FileConfiguration config) {
        COLLECT_DATA = config.getBoolean("collect-data", COLLECT_DATA);
        KIT_DATA_FOLDER = config.getString("kit-data-folder", KIT_DATA_FOLDER);
    }
}
