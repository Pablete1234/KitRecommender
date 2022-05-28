package me.pablete1234.kit.util.model;

import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import me.pablete1234.kit.util.Categories;
import me.pablete1234.kit.util.category.Category;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import tc.oc.pgm.kits.Slot;

import java.util.Iterator;
import java.util.Map;

public class CategorizedKitImpl implements KitPredictor.CategorizedKit {
    private final Category[] data = new Category[KIT_SIZE];

    public CategorizedKitImpl(Category[] cat) {
        System.arraycopy(cat, 0, data, 0, Math.min(cat.length, data.length));
    }

    public CategorizedKitImpl(Map<Slot, ItemStack> kit) {
        kit.forEach((s, i) -> data[s.getIndex()] = Categories.of(i.getType()));
    }

    public Multiset<Category> toMultiset() {
        Multiset<Category> categories = LinkedHashMultiset.create();
        for (Category d : data)
            if (d != null) categories.add(d);
        return categories;
    }

    @Override
    public Category get(int idx) {
        return data[idx];
    }

    @NotNull
    @Override
    public Iterator<Category> iterator() {
        return Iterators.forArray(data);
    }
}
