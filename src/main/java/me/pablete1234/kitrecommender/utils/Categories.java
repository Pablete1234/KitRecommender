package me.pablete1234.kitrecommender.utils;

import me.pablete1234.kitrecommender.utils.category.Bucket;
import me.pablete1234.kitrecommender.utils.category.Category;
import me.pablete1234.kitrecommender.utils.category.Tool;
import me.pablete1234.kitrecommender.utils.category.Weapon;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public abstract class Categories {

    private static final Map<Material, Category> CATEGORY_MAP;

    static {
        CATEGORY_MAP = new EnumMap<>(Material.class);
        add(Weapon.values());
        add(Tool.values());
        add(Bucket.INSTANCE);
    }

    private static void add(Category... categories) {
        for (Category category : categories) add(category);
    }

    private static void add(Category category) {
        for (Material mat : category.getAll()) CATEGORY_MAP.put(mat, category);
    }

    public static @Nullable Category of(Material mat) {
        return CATEGORY_MAP.get(mat);
    }

    public static boolean equal(Material m1, Material m2) {
        if (m1 == null || m2 == null) return m1 == m2;
        Category cat = of(m1);
        return cat != null && cat.getAll().contains(m2);
    }

}
