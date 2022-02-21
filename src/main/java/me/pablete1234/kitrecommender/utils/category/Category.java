package me.pablete1234.kitrecommender.utils.category;

import com.google.common.collect.ImmutableSet;
import me.pablete1234.kitrecommender.utils.CollectorUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Arrays;

public interface Category {

    ImmutableSet<Material> getAll();

    default byte getData(ItemStack is) {
        MaterialData md = is.getData();
        return md.getClass() == MaterialData.class ? 0 : md.getData();
    }

    default void putData(ItemStack is, byte data) {
        is.setData(is.getType().getNewData(data));
    }


    static ImmutableSet<Material> findMaterials(String ending) {
        return Arrays.stream(Material.values()).filter(m -> m.name().endsWith(ending))
                .collect(CollectorUtil.toImmutableSet());
    }

}
