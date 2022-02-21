package me.pablete1234.kitrecommender.utils.category;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public enum Tool implements EnchantmentCategory {
    PICKAXE,
    AXE,
    SPADE,
    HOE,
    SHEARS(ImmutableSet.of(Material.SHEARS));

    private static final Enchantment[] ENCHANTS = {Enchantment.DIG_SPEED, Enchantment.SILK_TOUCH, Enchantment.DURABILITY};
    private final ImmutableSet<Material> materials;

    Tool(ImmutableSet<Material> materials) {
        this.materials = materials;
    }

    Tool() {
        this.materials = Category.findMaterials("_" + name());
    }

    @Override
    public ImmutableSet<Material> getAll() {
        return materials;
    }

    @Override
    public Enchantment[] getEnchantments() {
        return ENCHANTS;
    }
}
