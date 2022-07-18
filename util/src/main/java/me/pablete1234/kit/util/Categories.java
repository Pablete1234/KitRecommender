package me.pablete1234.kit.util;

import me.pablete1234.kit.util.category.Block;
import me.pablete1234.kit.util.category.Bucket;
import me.pablete1234.kit.util.category.Category;
import me.pablete1234.kit.util.category.Consumable;
import me.pablete1234.kit.util.category.Item;
import me.pablete1234.kit.util.category.Tool;
import me.pablete1234.kit.util.category.Weapon;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

public abstract class Categories {

    private static final Map<Material, Category> CATEGORY_MAP = new EnumMap<>(Material.class);
    static {
        add(Weapon.values());
        add(Tool.values());
        add(Consumable.values());
        add(Bucket.INSTANCE, Block.INSTANCE);
    }

    private static void add(Category... categories) {
        for (Category category : categories) add(category);
    }

    private static void add(Category category) {
        for (Material mat : category.getAll()) CATEGORY_MAP.putIfAbsent(mat, category);
    }

    public static @NotNull Category of(Material mat) {
        return CATEGORY_MAP.computeIfAbsent(mat, Item::new);
    }

}
