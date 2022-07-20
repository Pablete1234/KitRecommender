package me.pablete1234.kit.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import me.pablete1234.kit.util.model.KitPredictor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.kits.Slot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface ItemKitWrapper {

    static ItemKitWrapper.PGM ofPGM(ItemKit kit) {
        return PGM.instances.computeIfAbsent(kit, ItemKitWrapper.PGM::new);
    }

    static ItemKitWrapper ofItems(Map<Slot, ItemStack> slotItems, List<ItemStack> freeItems) {
        return new Impl(slotItems, freeItems);
    }

    static void cleanup() {
        PGM.instances.clear();
    }

    Map<Slot, ItemStack> getSlotItems();

    List<ItemStack> getFreeItems();

    ImmutableSet<Material> getSimplifiedItems();

    default KitPredictor.CategorizedKit asCategorized() {
        return KitPredictor.CategorizedKit.of(getSlotItems());
    }

    class PGM implements ItemKitWrapper {
        private static final Map<ItemKit, ItemKitWrapper.PGM> instances = new HashMap<>();

        private final ItemKit kit;
        private final ImmutableSet<Material> simplified;
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

        public ImmutableSet<Material> getSimplifiedItems() {
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
        public ImmutableSet<Material> getSimplifiedItems() {
            return simplified;
        }
    }

}
