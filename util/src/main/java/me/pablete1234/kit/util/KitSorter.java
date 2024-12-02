package me.pablete1234.kit.util;

import com.google.common.collect.ImmutableSet;
import me.pablete1234.kit.util.category.Category;
import me.pablete1234.kit.util.model.KitPredictor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.pgm.kits.tag.ItemModifier;
import tc.oc.pgm.util.inventory.Slot;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @param <K> Kit, the type of kit used
 * @param <C> Container, the type of inventory used
 * @param <KI> Kit item, the item type used in kits
 * @param <CI> Container item, item type used in containers
 */
public class KitSorter<K, C, KI, CI> {

    // Duration in which changes are always considered preferences, since application of the kit.
    // Changes made within this period are uncontested and always considered intended
    // For changes made outside the period, the replacement items are further checked.
    public static final Duration PREFERENCE_DURATION = Duration.ofSeconds(15);

    public static final KitSorter<ItemKitWrapper, PlayerInventory, ItemStack, ItemStack> PGM =
            new KitSorter<>(new PGMAdapter());
    public static final KitSorter<ItemKitWrapper, ItemKitWrapper, ItemStack, ItemStack> KIT =
            new KitSorter<>(new KitAdapter());
    public static final KitSorter<InventoryImage, InventoryImage, Integer, Integer> IMAGE =
            new KitSorter<>(new InventoryImageAdapter());
    public static final KitSorter<ItemKitWrapper, KitPredictor.CategorizedKit, ItemStack, Category> PREDICTOR =
            new KitSorter<>(new PredictorAdapter());

    private final Adapter<K, C, KI, CI> adapter;

    public KitSorter(Adapter<K, C, KI, CI> adapter) {
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
    public boolean areSimilar(KI kit, CI item) {
        return kit != null && item != null && adapter.areSimilar(kit, item);
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
    public boolean isReplacement(K kit, KI kitItem, CI item, boolean lateEdit) {
        if (item == null) return false;
        Category c1 = adapter.getCategoryKI(kitItem), c2 = adapter.getCategoryCI(item);

        // If the material is the same, it's a good enough replacement. (eg: bow)
        // Or same category (eg: diamond sword replacing iron sword, or filled bucket replacing empty bucket)
        if (c1 == c2) return true;

        return lateEdit && adapter.doesntContainCI(kit, item);
    }

    /**
     * Performs the main logic of applying the preferences of the player, mutating
     * {@param slotItems} and {@param freeItems} according to what they prefer.
     *
     * @param inventory The player inventory
     * @param kit       The kit being edited
     * @param slotItems The slotted items
     * @param freeItems The free items
     * @param lateEdit  If the edit is not shortly after the kit is given
     */
    public void applyPreferences(C inventory,
                                 K kit,
                                 Map<Slot, KI> slotItems,
                                 List<KI> freeItems,
                                 boolean lateEdit) {
        Map<Slot, KI> potentiallyUnmapped = new HashMap<>();

        for (int i = 0; i < InventoryImage.PLAYER_SIZE; i++) {
            CI item = adapter.getItem(inventory, i);
            Slot slot = Slot.Player.forIndex(i);
            KI current = slotItems.get(slot);

            // No item, or item is not part of the kit. This is further checked later.
            if (item == null || adapter.doesntContainCI(kit, item)) {
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
            if (areSimilar(current, item)) continue;

            // Search, and pick-out the corresponding kit item
            KI kitItem = pollItem(inventory, slot, item, kit, slotItems, freeItems, potentiallyUnmapped, lateEdit);
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

    private KI pollItem(C inventory,
                        Slot currentSlot,
                        CI search,
                        K kit,
                        Map<Slot, KI> slotItems,
                        List<KI> freeItems,
                        Map<Slot, KI> potentiallyUnmapped,
                        boolean lateEdit) {

        Iterator<Map.Entry<Slot, KI>> slotIt = slotItems.entrySet().iterator();
        while (slotIt.hasNext()) {
            Map.Entry<Slot, KI> entry = slotIt.next();
            if (entry.getKey().getIndex() < currentSlot.getIndex()) continue;

            KI kitItem = entry.getValue();
            // Ignore this match if an item also exists in the designated slot in the kit
            // This is to prevent moving an item that shouldn't need to be moved.
            if (!areSimilar(kitItem, search) ||
                    areSimilar(kitItem, adapter.getItem(inventory, entry.getKey().getIndex()))) continue;
            slotIt.remove();
            return kitItem;
        }

        slotIt = potentiallyUnmapped.entrySet().iterator();
        while (slotIt.hasNext()) {
            Map.Entry<Slot, KI> entry = slotIt.next();
            KI kitItem = entry.getValue();
            if (!areSimilar(kitItem, search) ||
                    // Avoid moving an item from hot bar to pocket, if there's a loosely valid replacement in place
                    (entry.getKey() instanceof Slot.Player.Hotbar && currentSlot instanceof Slot.Player.Pockets &&
                            isReplacement(kit, kitItem, adapter.getItem(inventory, entry.getKey()), lateEdit)))
                continue;

            slotIt.remove();
            return kitItem;
        }

        Iterator<KI> itemIt = freeItems.iterator();
        while (itemIt.hasNext()) {
            KI kitItem = itemIt.next();
            if (!areSimilar(kitItem, search)) continue;
            itemIt.remove();
            return kitItem;
        }
        return null;
    }

    interface Adapter<K, C, KI, CI> {

        Category getCategoryKI(KI item);

        Category getCategoryCI(CI item);

        boolean areSimilar(KI kit, CI item);

        CI getItem(C container, int slot);

        default CI getItem(C container, Slot slot) {
            return getItem(container, slot.getIndex());
        }

        /**
         * Check if the item may be present in the kit
         *
         * @param kit the kit to check
         * @param item the item to check
         * @return true if the item could be present in the kit, false if it definitely is not in the kit
         */
        boolean maybeContainsKI(K kit, KI item);

        boolean maybeContainsCI(K kit, CI item);

        /**
         * Check if the item is definitely not present in the kit
         * @param kit the kit to check
         * @param item the item to check
         * @return true if the item is definitely not present in the kit, false if it could be present in the kit
         */
        default boolean doesntContainKI(K kit, KI item) {
            return !maybeContainsKI(kit, item);
        }

        default boolean doesntContainCI(K kit, CI item) {
            return !maybeContainsCI(kit, item);
        }
    }

    interface SimpleAdapter<K, C, I> extends Adapter<K, C, I, I> {
        @Override
        default Category getCategoryKI(I item) {
            return getCategory(item);
        }

        @Override
        default Category getCategoryCI(I item) {
            return getCategory(item);
        }

        Category getCategory(I item);

        @Override
        default boolean maybeContainsKI(K kit, I item) {
            return maybeContains(kit, item);
        }

        @Override
        default boolean maybeContainsCI(K kit, I item) {
            return maybeContains(kit,item);
        }

        boolean maybeContains(K kit, I item);
    }

    private static abstract class BukkitAdapter<I> implements SimpleAdapter<ItemKitWrapper, I, ItemStack> {
        @Override
        public Category getCategory(ItemStack item) {
            return Categories.of(item.getType());
        }

        @Override
        public boolean areSimilar(ItemStack kit, ItemStack item) {
            return kit.getType() == item.getType() &&
                    // If material has durability (eg: sword), ignore. Otherwise (eg: wool color) check durability.
                    (kit.getType().getMaxDurability() > 0 ||
                            ItemModifier.TEAM_COLOR.has(kit) ||
                            kit.getDurability() == item.getDurability()) &&
                    kit.getEnchantments().equals(item.getEnchantments());
        }

        @Override
        public boolean maybeContains(ItemKitWrapper kit, ItemStack item) {
            return kit.getMaterials().contains(item.getType());
        }
    }


    private static class PGMAdapter extends BukkitAdapter<PlayerInventory> {
        @Override
        public ItemStack getItem(PlayerInventory container, int slot) {
            return container.getItem(slot);
        }
    }

    private static class KitAdapter extends BukkitAdapter<ItemKitWrapper> {
        @Override
        public ItemStack getItem(ItemKitWrapper ik, int slot) {
            return ik.getSlotItems().get(Slot.Player.forIndex(slot));
        }
    }

    public static class InventoryImageAdapter implements SimpleAdapter<InventoryImage, InventoryImage, Integer> {

        @Override
        public Category getCategory(Integer item) {
            return Categories.of(InventoryImage.getMaterial(item));
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

    public static class PredictorAdapter implements Adapter<ItemKitWrapper, KitPredictor.CategorizedKit, ItemStack, Category> {
        @Override
        public Category getCategoryKI(ItemStack item) {
            return Categories.of(item.getType());
        }

        @Override
        public Category getCategoryCI(Category item) {
            return item;
        }

        @Override
        public boolean areSimilar(ItemStack kit, Category item) {
            return item.getAll().contains(kit.getType());
        }

        @Override
        public Category getItem(KitPredictor.CategorizedKit container, int slot) {
            return container.get(slot);
        }

        @Override
        public boolean maybeContainsKI(ItemKitWrapper kit, ItemStack item) {
            return kit.getMaterials().contains(item);
        }

        @Override
        public boolean maybeContainsCI(ItemKitWrapper kit, Category item) {
            ImmutableSet<Material> items = item.getAll();
            for (Material mat : kit.getMaterials())
                if (items.contains(mat)) return true;
            return false;
        }
    }

}
