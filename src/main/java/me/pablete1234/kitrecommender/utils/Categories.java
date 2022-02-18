package me.pablete1234.kitrecommender.utils;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public abstract class Categories {

    private static final Map<Material, Category> CATEGORY_MAP;

    static {
        CATEGORY_MAP = new EnumMap<>(Material.class);
        add(Category.Weapon.values());
        add(Category.Tool.values());
        add(Category.Bucket.INSTANCE);
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
