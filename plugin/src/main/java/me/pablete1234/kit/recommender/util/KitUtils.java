package me.pablete1234.kit.recommender.util;

import me.pablete1234.kit.recommender.modifiers.PlayerKitModel;
import me.pablete1234.kit.util.ItemKitWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.pgm.kits.Slot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitUtils {

    public static boolean isEmpty(PlayerInventory inv) {
        for (int i = 0; i < inv.getSize(); i++) if (inv.getItem(i) != null) return false;
        return true;
    }

    /**
     * Find all kits that are being given as part of the same transaction as displaced items
     * @param appliedKits collection with all the kit models applied on the player at thi time
     * @param displacedItems the displaced item collection used in the event
     * @return list with all matching player kit models
     */
    public static List<PlayerKitModel> findJointKits(
            Collection<PlayerKitModel> appliedKits, List<ItemStack> displacedItems) {
        if (displacedItems == null) return new ArrayList<>();
        return appliedKits.stream()
                .filter(k -> k.getLastDisplacedItems() == displacedItems)
                .collect(Collectors.toList());
    }

    /**
     * Merge multiple kit models' kits into one condensed item kit wrapper
     * @param otherKits lost of other kits
     * @param kit the extra new kit to be given
     * @return a dummy ItemKitWrapper with guaranteed mutable map & list of items, and a null kit.
     */
    public static ItemKitWrapper condenseKits(List<PlayerKitModel> otherKits, ItemKitWrapper kit) {
        Map<Slot, ItemStack> condensedItems = new HashMap<>();
        List<ItemStack> condensedFree = new ArrayList<>();
        for (PlayerKitModel kitModel : otherKits) {
            condensedItems.putAll(kitModel.getKit().getSlotItems());
            condensedFree.addAll(kitModel.getKit().getFreeItems());
        }
        condensedItems.putAll(kit.getSlotItems());
        condensedFree.addAll(kit.getFreeItems());

        return ItemKitWrapper.ofInventory(condensedItems, condensedFree);
    }

}
