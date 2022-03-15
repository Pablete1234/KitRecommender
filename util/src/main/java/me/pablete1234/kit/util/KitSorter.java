package me.pablete1234.kit.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.pgm.kits.Slot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KitSorter<I, C, K> {

    public static final KitSorter<ItemStack, PlayerInventory, ItemKitWrapper> PGM = new KitSorter<>(new BukkitAdapter());
    public static final KitSorter<Integer, InventoryImage, InventoryImage> IMAGE = new KitSorter<>(new InventoryImageAdapter());

    private final Adapter<I, C, K> adapter;

    public KitSorter(Adapter<I, C, K> adapter) {
        this.adapter = adapter;
    }

    /**
     * Checks if two items are similar. Similar means they are the same type of item,
     * but allow different quantity or durability.
     *
     * @param kit  The item in the kit
     * @param item The item in the player inventory
     * @return true if the items are similar, false otherwise
     */
    public boolean areSimilar(I kit, I item) {
        return item != null && adapter.areSimilar(kit, item);
    }

    /**
     * Check if an item is being replaced by a "better" item (versus being moved for preference reasons).
     * This is defined as an upgrade or replacement.
     * Examples:
     * - Diamond sword - Iron sword
     * - Diamond helmet - Iron helmet
     * - Bow with different enchantments
     * - Water bucket - Empty bucket
     * - Random material not in the kit, is a replacement or not based on {@param lateEdit}
     *
     * @param kit      The kit being edited
     * @param kitItem  The item in the kit
     * @param item     The item in the player inventory
     * @param lateEdit if the edit is not right after the kit is given
     * @return true if {@param item} is probably a replacement for {@param kit},
     * false if we think this is a player preference difference.
     */
    public boolean isReplacement(K kit, I kitItem, I item, boolean lateEdit) {
        if (item == null) return false;
        Material m1 = adapter.getMaterial(kitItem), m2 = adapter.getMaterial(item);

        // If the material is the same, it's a good enough replacement. (eg: bow)
        // Or same category (eg: diamond sword replacing iron sword, or filled bucket replacing empty bucket)
        if (m1 == m2 || Categories.equal(m1, m2)) return true;

        return lateEdit && adapter.doesntContain(kit, item);
    }

    /**
     * Performs the main logic of learning the preferences of the player, mutating
     * {@param slotItems} and {@param freeItems} according to what they prefer.
     *
     * @param inventory The player inventory
     * @param kit       The kit being edited
     * @param slotItems The slotted items
     * @param freeItems The free items
     * @param lateEdit  If the edit is not shortly after the kit is given
     */
    public void learnPreferences(C inventory,
                                 K kit,
                                 Map<Slot, I> slotItems,
                                 List<I> freeItems,
                                 boolean lateEdit) {
        Map<Slot, I> potentiallyUnmapped = new HashMap<>();

        for (int i = 0; i < InventoryImage.PLAYER_SIZE; i++) {
            I item = adapter.getItem(inventory, i);
            Slot slot = Slot.Player.forIndex(i);
            I current = slotItems.get(slot);

            // No item, or item is not part of the kit. This is further checked later.
            if (item == null || adapter.doesntContain(kit, item)) {
                // An item in the kit exists for that slot, pollItem would not be able to find it if we
                // just leave it here.
                // The solution is to move the item to potentiallyUnmapped, and if nothing claims it, it will
                // end up back to slotItems at the end.
                if (current != null) {
                    potentiallyUnmapped.put(slot, current);
                    slotItems.remove(slot);
                }
                continue;
            }

            // Item is already in the desired slot
            if (areSimilar(item, current)) continue;

            // Search, and pick-out the corresponding kit item
            I kitItem = pollItem(inventory, slot, item, kit, slotItems, freeItems, potentiallyUnmapped, lateEdit);
            // Apparently the item isn't in the forward part of the kit, it may already have been mapped
            // and there are multiple items of the same type in the kit.
            if (kitItem == null) {
                // The same way as before, because the slot does not map to the kit, we need to move the item
                // to potentiallyUnmapped, and if nothing claims it, it will end up back to slotItems at the end.
                if (current != null) {
                    potentiallyUnmapped.put(slot, current);
                    slotItems.remove(slot);
                }
                continue;
            }

            // Displace the current item, a later iteration will put it in another slot... if necessary
            if (current != null) freeItems.add(current);
            slotItems.put(slot, kitItem);
        }
        potentiallyUnmapped.forEach((slot, item) -> {
            if (slotItems.containsKey(slot)) freeItems.add(item);
            else slotItems.put(slot, item);
        });
    }

    private I pollItem(C inventory,
                       Slot currentSlot,
                       I search,
                       K kit,
                       Map<Slot, I> slotItems,
                       List<I> freeItems,
                       Map<Slot, I> potentiallyUnmapped,
                       boolean lateEdit) {

        Iterator<Map.Entry<Slot, I>> slotIt = slotItems.entrySet().iterator();
        while (slotIt.hasNext()) {
            Map.Entry<Slot, I> entry = slotIt.next();
            if (entry.getKey().getIndex() < currentSlot.getIndex()) continue;

            I kitItem = entry.getValue();
            // Ignore this match if an item also exists in the designated slot in the kit
            // This is to prevent moving an item that shouldn't need to be moved.
            if (!areSimilar(kitItem, search) ||
                    areSimilar(kitItem, adapter.getItem(inventory, entry.getKey().getIndex()))) continue;
            slotIt.remove();
            return kitItem;
        }

        slotIt = potentiallyUnmapped.entrySet().iterator();
        while (slotIt.hasNext()) {
            Map.Entry<Slot, I> entry = slotIt.next();
            I kitItem = entry.getValue();
            if (!areSimilar(kitItem, search) ||
                    // Avoid moving an item from hot bar to pockets, if there's a loosely valid replacement in place
                    (entry.getKey() instanceof Slot.Player.Hotbar && currentSlot instanceof Slot.Player.Pockets &&
                            isReplacement(kit, kitItem, adapter.getItem(inventory, entry.getKey()), lateEdit)))
                continue;

            slotIt.remove();
            return kitItem;
        }

        Iterator<I> itemIt = freeItems.iterator();
        while (itemIt.hasNext()) {
            I kitItem = itemIt.next();
            if (!areSimilar(kitItem, search)) continue;
            itemIt.remove();
            return kitItem;
        }
        return null;
    }

    interface Adapter<I, C, K> {

        Material getMaterial(I item);

        boolean areSimilar(I kit, I item);

        I getItem(C container, int slot);

        default I getItem(C container, Slot slot) {
            return getItem(container, slot.getIndex());
        }

        /**
         * Check if the item may be present in the kit
         *
         * @param kit the kit to check
         * @param item the item to check
         * @return true if the item could be present in the kit, false if it definitely is not in the kit
         */
        boolean maybeContains(K kit, I item);

        /**
         * Check if the item is definitely not present in the kit
         * @param kit the kit to check
         * @param item the item to check
         * @return true if the item is definitely not present in the kit, false if it could be present in the kit
         */
        default boolean doesntContain(K kit, I item) {
            return !maybeContains(kit, item);
        }
    }


    private static class BukkitAdapter implements Adapter<ItemStack, PlayerInventory, ItemKitWrapper> {
        @Override
        public Material getMaterial(ItemStack item) {
            return item.getType();
        }

        @Override
        public boolean areSimilar(ItemStack kit, ItemStack item) {
            return kit.getType() == item.getType() &&
                    // If material has durability (eg: sword), ignore. Otherwise (eg: wool color) check durability.
                    (kit.getType().getMaxDurability() > 0 || kit.getDurability() == item.getDurability()) &&
                    kit.getEnchantments().equals(item.getEnchantments());
        }

        @Override
        public ItemStack getItem(PlayerInventory container, int slot) {
            return container.getItem(slot);
        }

        @Override
        public boolean maybeContains(ItemKitWrapper kit, ItemStack item) {
            return kit.maybeContains(item);
        }
    }

    public static class InventoryImageAdapter implements Adapter<Integer, InventoryImage, InventoryImage> {

        @Override
        public Material getMaterial(Integer item) {
            return InventoryImage.getMaterial(item);
        }

        @Override
        public boolean areSimilar(Integer kit, Integer item) {
            // Because of how we encode items, there are 2 possible scenarios:
            //  - The item is categorized: the "data" section is the enchantments, and we want to use that to compare
            //  - The item is not categorized: the "data" section is material data (eg: wool color)
            // In both of those cases we want them to match, we don't need to ignore durability for tools like the
            // item-stack based approach needs to, because we don't store tool durability, since we encode
            // enchantments on that data section instead

            // Ignore amount, ensure type and data match
            return (kit & ~InventoryImage.AMOUNT_MASK) == (item & ~InventoryImage.AMOUNT_MASK);
        }

        @Override
        public Integer getItem(InventoryImage container, int slot) {
            return container.getItem(slot);
        }

        @Override
        public boolean maybeContains(InventoryImage kit, Integer item) {
            return kit.maybeContains(item);
        }
    }

}
