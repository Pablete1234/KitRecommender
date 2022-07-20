package me.pablete1234.kit.util;

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

public class ItemKitWrapper {
    private static final Map<ItemKit, ItemKitWrapper> instances = new HashMap<>();

    public static ItemKitWrapper of(ItemKit kit) {
        return instances.computeIfAbsent(kit, ItemKitWrapper::new);
    }

    public static ItemKitWrapper ofInventory(Map<Slot, ItemStack> slotItems, List<ItemStack> freeItems) {
        return new ItemKitWrapper(null, slotItems, freeItems);
    }

    public static void cleanup() {
        instances.clear();
    }

    private final ItemKit kit;
    private final Map<Slot, ItemStack> slotItems;
    private final List<ItemStack> freeItems;
    private final ImmutableSet<Material> simplified; // FIXME: Potentially use a bloom filter
    private final KitPredictor.CategorizedKit categorized;

    private ItemKitWrapper(ItemKit kit) {
        this(kit, kit.getSlotItems(), kit.getFreeItems());
    }

    private ItemKitWrapper(ItemKit kit, Map<Slot, ItemStack> slotItems, List<ItemStack> freeItems) {
        this.kit = kit;
        this.slotItems = slotItems;
        this.freeItems = freeItems;
        this.simplified = Stream.concat(slotItems.values().stream(), freeItems.stream())
                .map(ItemStack::getType)
                .collect(CollectorUtil.toImmutableSet());
        this.categorized = KitPredictor.CategorizedKit.of(slotItems);
    }

    public ItemKit getPGMKit() {
        return kit;
    }

    public Map<Slot, ItemStack> getSlotItems() {
        return slotItems;
    }

    public List<ItemStack> getFreeItems() {
        return freeItems;
    }

    public ImmutableSet<Material> getSimplifiedItems() {
        return simplified;
    }

    public boolean maybeContains(ItemStack is) {
        return simplified.contains(is.getType());
    }

    public boolean doesntContain(ItemStack is) {
        return !simplified.contains(is.getType());
    }

    public KitPredictor.CategorizedKit asCategorized() {
        return categorized;
    }

}
