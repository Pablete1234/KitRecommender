package me.pablete1234.kitrecommender.engine;

import me.pablete1234.kitrecommender.utils.ItemKitWrapper;

import java.util.UUID;

/**
 * Tasked with creating KitModifiers trained for the specific player.
 */
public interface KitModifierFactory {

    KitModifier create(UUID player, ItemKitWrapper kit);

}
