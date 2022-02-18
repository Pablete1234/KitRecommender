package me.pablete1234.kitrecommender.utils;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;

import java.util.Arrays;

public interface Category {

    ImmutableSet<Material> getAll();

    enum Weapon implements Category {
        SWORD(Arrays.stream(Material.values())
                .filter(m -> m.name().endsWith("_SWORD"))
                .collect(CollectorUtil.toImmutableSet())),
        BOW(ImmutableSet.of(Material.BOW));

        private final ImmutableSet<Material> materials;

        Weapon(ImmutableSet<Material> materials) {
            this.materials = materials;
        }

        @Override
        public ImmutableSet<Material> getAll() {
            return materials;
        }
    }

    enum Tool implements Category {
        PICKAXE, AXE, SPADE, HOE;

        private final ImmutableSet<Material> materials;

        Tool() {
            this.materials = Arrays.stream(Material.values())
                    .filter(m -> m.name().endsWith("_" + name()))
                    .collect(CollectorUtil.toImmutableSet());
        }

        @Override
        public ImmutableSet<Material> getAll() {
            return materials;
        }
    }

    class Bucket implements Category {
        public static final Bucket INSTANCE = new Bucket();

        private final ImmutableSet<Material> materials = ImmutableSet.of(
                Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET);

        private Bucket() {}

        @Override
        public ImmutableSet<Material> getAll() {
            return materials;
        }
    }

}
