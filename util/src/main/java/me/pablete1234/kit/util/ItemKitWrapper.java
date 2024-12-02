package me.pablete1234.kit.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import me.pablete1234.kit.util.model.KitPredictor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.util.inventory.Slot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface ItemKitWrapper {

    /**
     * Create a pgm-backed item kit wrapper, these will use shared instances and is guaranteed to be immutable.
     * @param kit the PGM kit the wrapper is based on
     * @return an item kit wrapper for a pgm kit, shared instance if one exists
     */
    static ItemKitWrapper.PGM ofPGM(ItemKit kit) {
        return PGM.instances.computeIfAbsent(kit, ItemKitWrapper.PGM::new);
    }

    /**
     * Creates an item-backed item kit wrapper, this is guaranteed to be a new instance, no immutability is imposed.
     * @param slotItems map with the slotted items on the lit
     * @param freeItems list with the free items on the kit
     * @return an item kit wrapper for the specific map and list, always a new instance
     */
    static ItemKitWrapper ofItems(Map<Slot, ItemStack> slotItems, List<ItemStack> freeItems) {
        return new Impl(slotItems, freeItems);
    }

    /**
     * Cleanup the list of pgm-kit-backed instances of item kit wrapper.
     * This is called after a map finishes playing to clean-up past state.
     */
    static void cleanup() {
        PGM.instances.clear();
    }

    /**
     * @return Map with the items that belong in a specific slot
     */
    Map<Slot, ItemStack> getSlotItems();

    /**
     * @return List with items not assigned to a specific slot
     */
    List<ItemStack> getFreeItems();

    /**
     * Set of materials contained in the kit.
     * This should be used only for optimization purposes, if the material is not in this set then the kit is
     * guaranteed not to contain that material, otherwise, it may or may not contain the item itself.
     * @return an immutable set of materials used anywhere in the kit
     */
    ImmutableSet<Material> getMaterials();

    /**
     * Convert the kit to a categorized kit, potentially returning a cached instance since categorized kit is immutable
     * @return a categorized kit with the items of this item kit wrapper
     */
    default KitPredictor.CategorizedKit asCategorized() {
        return KitPredictor.CategorizedKit.of(getSlotItems());
    }

    class PGM implements ItemKitWrapper {
        private static final Map<ItemKit, ItemKitWrapper.PGM> instances = new HashMap<>();

        private final ItemKit kit;
        private final ImmutableSet<Material> simplified;
        // Because PGM kit wrappers are reused, keep a cached categorized copy to avoid creating one per player
        private final KitPredictor.CategorizedKit categorized;

        private PGM(ItemKit kit) {
            this.kit = kit;
            this.simplified = Impl.toSimplified(kit.getSlotItems(), kit.getFreeItems());
            this.categorized = KitPredictor.CategorizedKit.of(kit.getSlotItems());
        }

        public ItemKit getPGMKit() {
            return kit;
        }

        public ImmutableMap<Slot, ItemStack> getSlotItems() {
            return kit.getSlotItems();
        }

        public List<ItemStack> getFreeItems() {
            return kit.getFreeItems();
        }

        public ImmutableSet<Material> getMaterials() {
            return simplified;
        }


        public KitPredictor.CategorizedKit asCategorized() {
            return categorized;
        }

    }

    class Impl implements ItemKitWrapper {
        private final Map<Slot, ItemStack> slotItems;
        private final List<ItemStack> freeItems;
        private final ImmutableSet<Material> simplified;

        private Impl(Map<Slot, ItemStack> slotItems, List<ItemStack> freeItems) {
            this.slotItems = slotItems;
            this.freeItems = freeItems;
            this.simplified = toSimplified(slotItems, freeItems);
        }

        private static ImmutableSet<Material> toSimplified(Map<Slot, ItemStack> slotItems, List<ItemStack> freeItems) {
            return Stream.concat(slotItems.values().stream(), freeItems.stream())
                    .map(ItemStack::getType)
                    .collect(CollectorUtil.toImmutableSet());
        }

        @Override
        public Map<Slot, ItemStack> getSlotItems() {
            return slotItems;
        }

        @Override
        public List<ItemStack> getFreeItems() {
            return freeItems;
        }

        @Override
        public ImmutableSet<Material> getMaterials() {
            return simplified;
        }
    }

}
