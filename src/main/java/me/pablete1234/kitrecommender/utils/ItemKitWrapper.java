package me.pablete1234.kitrecommender.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.kits.Slot;

import java.util.stream.Stream;

public class ItemKitWrapper {
    private final ItemKit kit;
    private final ImmutableSet<ItemStack> simplified;

    public ItemKitWrapper(ItemKit kit) {
        this.kit = kit;
        this.simplified = Stream.concat(kit.getSlotItems().values().stream(), kit.getFreeItems().stream())
                .map(ItemKitWrapper::simplifyItem)
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

    public ImmutableSet<ItemStack> getSimplifiedItems() {
        return simplified;
    }

    public boolean containsItem(ItemStack is) {
        return simplified.contains(simplifyItem(is));
    }

    public static ItemStack simplifyItem(ItemStack item) {
        if (item.getAmount() == item.getMaxStackSize()) return item;
        ItemStack is = item.clone();
        is.setAmount(item.getMaxStackSize());
        return is;
    }
}
