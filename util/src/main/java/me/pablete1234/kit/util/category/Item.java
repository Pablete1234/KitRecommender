package me.pablete1234.kit.util.category;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;

public class Item implements Category {

  private final Material material;

  public Item(Material material) {
    this.material = material;
  }

  @Override
  public ImmutableSet<Material> getAll() {
    return ImmutableSet.of(material);
  }

  @Override
  public String toString() {
    return material.toString();
  }
}
