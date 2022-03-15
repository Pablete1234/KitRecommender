package me.pablete1234.kit.util.category;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public enum Weapon implements EnchantmentCategory {
    SWORD(Category.findMaterials("_SWORD"),
            Enchantment.DAMAGE_ALL, Enchantment.KNOCKBACK, Enchantment.FIRE_ASPECT),
    BOW(ImmutableSet.of(Material.BOW),
            Enchantment.ARROW_DAMAGE, Enchantment.ARROW_KNOCKBACK, Enchantment.ARROW_FIRE, Enchantment.ARROW_INFINITE);

    private final ImmutableSet<Material> materials;
    private final Enchantment[] enchants;

    Weapon(ImmutableSet<Material> materials, Enchantment... enchants) {
        this.materials = materials;
        this.enchants = enchants;
    }

    @Override
    public ImmutableSet<Material> getAll() {
        return materials;
    }

    @Override
    public Enchantment[] getEnchantments() {
        return enchants;
    }

}
