package me.pablete1234.kit.util.category;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;

public enum Consumable implements Category {
  SPECIAL(ImmutableSet.of(Material.GOLDEN_APPLE, Material.POTION)),
  FOOD(Category.findMaterials(Material::isEdible));

  private final ImmutableSet<Material> materials;

  Consumable(ImmutableSet<Material> materials) {
    this.materials = materials;
  }

  @Override
  public ImmutableSet<Material> getAll() {
    return materials;
  }

}
