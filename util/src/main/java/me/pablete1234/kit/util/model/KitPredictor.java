package me.pablete1234.kit.util.model;

import com.google.common.collect.Multiset;
import me.pablete1234.kit.util.ItemKitWrapper;
import me.pablete1234.kit.util.KitSorter;
import me.pablete1234.kit.util.category.Category;
import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.kits.Slot;

import java.util.List;
import java.util.Map;

public interface KitPredictor {

    default void predictKit(ItemKitWrapper kit,
                            Map<Slot, ItemStack> slotItems,
                            List<ItemStack> freeItems) {
        CategorizedKit original = kit.asCategorized();
        CategorizedKit predicted = predictKit(original);
        // Same instance, no change. Categorized kit is immutable.
        if (predicted == original) return;

        KitSorter.PREDICTOR.applyPreferences(predicted, kit, slotItems, freeItems, false);
    }

    default void learn(ItemKitWrapper kit,
                       Map<Slot, ItemStack> slotItems,
                       List<ItemStack> freeItems) {
        // Yes, free items are being thrown away... not much I can do about it
        CategorizedKit original = kit.asCategorized();
        CategorizedKit modified = CategorizedKit.of(slotItems);

        learn(original, modified);
    }

    CategorizedKit predictKit(CategorizedKit kit);
    void learn(CategorizedKit kit, CategorizedKit preference);

    interface CategorizedKit extends Iterable<Category> {
        int KIT_SIZE = 36;

        static CategorizedKit of(Category[] kit) {
            return new CategorizedKitImpl(kit);
        }
        static CategorizedKit of(Map<Slot, ItemStack> kit) {
            return new CategorizedKitImpl(kit);
        }

        Category get(int i);
        Multiset<Category> toMultiset();

        default int size() {
            return KIT_SIZE;
        }
    }
}
