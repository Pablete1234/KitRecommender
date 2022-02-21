package me.pablete1234.kitrecommender.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.kits.Slot;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ItemKitWrapper {
    private static final Map<ItemKit, ItemKitWrapper> instances = new HashMap<>();

    public static ItemKitWrapper of(ItemKit kit) {
        return instances.computeIfAbsent(kit, ItemKitWrapper::new);
    }

    public static void cleanup() {
        instances.clear();
    }

    private final ItemKit kit;
    private final ImmutableSet<Material> simplified; // FIXME: Potentially use a bloom filter

    private ItemKitWrapper(ItemKit kit) {
        this.kit = kit;
        this.simplified = Stream.concat(kit.getSlotItems().values().stream(), kit.getFreeItems().stream())
                .map(ItemStack::getType)
                .collect(CollectorUtil.toImmutableSet());
    }

    public ItemKit getKit() {
        return kit;
    }

    public ImmutableMap<Slot, ItemStack> getSlotItems() {
        return kit.getSlotItems();
    }

    public ImmutableList<ItemStack> getFreeItems() {
        return kit.getFreeItems();
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

}
