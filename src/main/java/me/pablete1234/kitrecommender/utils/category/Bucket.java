package me.pablete1234.kitrecommender.utils.category;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;

public class Bucket implements Category {
    public static final Bucket INSTANCE = new Bucket();

    private final ImmutableSet<Material> materials = ImmutableSet.of(
            Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET);

    private Bucket() {
    }

    @Override
    public ImmutableSet<Material> getAll() {
        return materials;
    }
}
