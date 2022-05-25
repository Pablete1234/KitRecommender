package me.pablete1234.kit.util.category;

import com.google.common.collect.ImmutableSet;
import me.pablete1234.kit.util.CollectorUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.function.Predicate;

public interface Category {

    ImmutableSet<Material> getAll();

    default byte getData(ItemStack is) {
        MaterialData md = is.getData();
        return md.getClass() == MaterialData.class ? 0 : md.getData();
    }

    default void putData(ItemStack is, byte data) {
        //noinspection deprecation
        is.setData(is.getType().getNewData(data));
    }

    static ImmutableSet<Material> findMaterials(String ending) {
        return findMaterials(m -> m.name().endsWith(ending));
    }

    static ImmutableSet<Material> findMaterials(Predicate<Material> test) {
        return Arrays.stream(Material.values()).filter(test).collect(CollectorUtil.toImmutableSet());
    }

}
