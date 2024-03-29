package me.pablete1234.kit.recommender.itf;

import org.bukkit.event.inventory.InventoryCloseEvent;
import tc.oc.pgm.kits.ApplyItemKitEvent;

import java.util.UUID;

/**
 * A kit modifier that can both adjust kits being applied, and learn from changes to player inventories.
 */
public interface KitModifier {

    /**
     * A kit is being applied on a player, re-order it
     * @param event the event to modify
     */
    void adjustKit(ApplyItemKitEvent event);

    /**
     * A player has closed their inventory, learn from their changes
     * @param event the event to learn from
     * @return true if there was anything to learn from, false otherwise
     */
    boolean learnPreferences(InventoryCloseEvent event);

    /**
     * Clean what kits are applied to this player
     * This is called when a player dies, or their inventory gets cleared.
     * Downstream kit modifiers should stop assuming the given player has any kits applied
     *
     * @param player the player to clean
     */
    void cleanKits(UUID player);

    /**
     * The player has left the game, clean up everything
     * This is called when a player leaves the game, or the match cycles to the next map.
     *
     * @param player the leaving player
     */
    void cleanup(UUID player);

    /**
     * Match has finished, cleanup all state
     */
    void cleanup();
}
