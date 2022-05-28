package me.pablete1234.kit.recommender.itf;

import java.util.UUID;

/**
 * Simpler version of the kit modifier, which does not clean any state.
 * This is useful for already kit-specific models that do not proxy or hold any state.
 */
public interface SimpleKitModifier extends KitModifier {

    @Override
    default void cleanKits(UUID player) {}

    @Override
    default void cleanup(UUID player) {
        cleanup();
    }

    @Override
    default void cleanup() {}
}
