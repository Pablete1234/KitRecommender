package me.pablete1234.kit.util.category;

import com.google.common.collect.ImmutableSet;
import me.pablete1234.kit.util.CollectorUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Represents a category of item, eg: swords, blocks, buckets.
 * The idea is a player will treat all items of the same category similarly, different
 * implementations of the interface exist for the different categories.
 * See {@link me.pablete1234.kit.util.Categories}
 */
public interface Category {

    ImmutableSet<Material> getAll();

    @SuppressWarnings("deprecation")
    default byte getData(ItemStack is) {
        MaterialData md = is.getData();
        return md.getClass() == MaterialData.class ? 0 : md.getData();
    }

    @SuppressWarnings("deprecation")
    default void putData(ItemStack is, byte data) {
        is.setData(is.getType().getNewData(data));
    }

    /**
     * Get the id of the first material on the category, which should remain unique.
     * @return an id that is unique for each category
     */
    @SuppressWarnings("deprecation")
    default int getFirstId() {
        return getAll().iterator().next().getId();
    }

    static ImmutableSet<Material> findMaterials(String ending) {
        return findMaterials(m -> m.name().endsWith(ending));
    }

    static ImmutableSet<Material> findMaterials(Predicate<Material> test) {
        return Arrays.stream(Material.values()).filter(test).collect(CollectorUtil.toImmutableSet());
    }

    default String toHumanString() {
        return toString().toLowerCase(Locale.ROOT).replace("_", " ");
    }

}
