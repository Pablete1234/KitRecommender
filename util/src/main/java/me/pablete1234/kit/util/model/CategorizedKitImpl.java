package me.pablete1234.kit.util.model;

import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import me.pablete1234.kit.util.Categories;
import me.pablete1234.kit.util.category.Category;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import tc.oc.pgm.util.inventory.Slot;

import java.util.Iterator;
import java.util.Map;

public class CategorizedKitImpl implements KitPredictor.CategorizedKit {
    private final Category[] data = new Category[KIT_SIZE];

    private Multiset<Category> multiset;

    public CategorizedKitImpl(Category[] cat) {
        System.arraycopy(cat, 0, data, 0, Math.min(cat.length, data.length));
    }

    public CategorizedKitImpl(Map<Slot, ItemStack> kit) {
        kit.forEach((s, i) -> {
            if (s.hasIndex() && s.getIndex() < KIT_SIZE)
                data[s.getIndex()] = Categories.of(i.getType());
        });
    }

    public Multiset<Category> toMultiset() {
        if (multiset == null) {
            // Cache the multiset
            this.multiset = LinkedHashMultiset.create();
            for (Category d : data)
                if (d != null) this.multiset.add(d);
        }
        // And return a copy (for mutability not to affect it)
        return LinkedHashMultiset.create(multiset);
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
