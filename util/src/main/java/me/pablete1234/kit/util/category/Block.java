package me.pablete1234.kit.util.category;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;

public class Block implements Category {
    public static final Block INSTANCE = new Block();

    private final ImmutableSet<Material> materials = Category.findMaterials(Material::isBlock);

    private Block() {
    }

    @Override
    public ImmutableSet<Material> getAll() {
        return materials;
    }
}
