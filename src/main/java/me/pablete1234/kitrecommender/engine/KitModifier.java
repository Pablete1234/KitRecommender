package me.pablete1234.kitrecommender.engine;

import org.bukkit.event.inventory.InventoryCloseEvent;
import tc.oc.pgm.kits.ApplyItemKitEvent;

/**
 * A kit modifier that can both adjust kits being applied, and learn from changes to player inventories.
 */
public interface KitModifier {

    void adjustKit(ApplyItemKitEvent event);

    void learnPreferences(InventoryCloseEvent event);

}
