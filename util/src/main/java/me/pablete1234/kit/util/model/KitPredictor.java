package me.pablete1234.kit.util.model;

import com.google.common.collect.Multiset;
import me.pablete1234.kit.util.category.Category;
import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.kits.Slot;

import java.util.Map;

public interface KitPredictor {

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

        @SuppressWarnings("SameReturnValue")
        default int size() {
            return KIT_SIZE;
        }
    }
}
