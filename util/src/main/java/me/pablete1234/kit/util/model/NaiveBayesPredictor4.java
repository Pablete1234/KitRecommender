package me.pablete1234.kit.util.model;

import com.google.common.collect.Multiset;
import me.pablete1234.kit.util.category.Category;
import me.pablete1234.kit.util.category.Tool;
import me.pablete1234.kit.util.category.Weapon;
import me.pablete1234.kit.util.matrix.Matrix;
import me.pablete1234.kit.util.matrix.Row;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NaiveBayesPredictor4 implements KitPredictor {

    private final Map<Category, Matrix> chances;

    public NaiveBayesPredictor4(Map<Category, Matrix> chances) {
        this.chances = chances;
    }

    public Map<Category, Matrix> getChances() {
        return chances;
    }

    public boolean ignoreKit(CategorizedKit kit) {
        for (Category c : kit)
            if (c instanceof Weapon || c instanceof Tool) return false;
        return true;
    }

    public CategorizedKit predictKit(CategorizedKit kit) {
        if (ignoreKit(kit)) return kit;

        Multiset<Category> bag = kit.toMultiset();
        if (bag.size() <= 1) return kit;


        Map<Category, Row> values = new LinkedHashMap<>();
        for (Category category : bag.elementSet())
            values.put(category, this.predict(kit, category));


        Category[] prediction = new Category[36];

        while (!bag.isEmpty()) {
            Category cat = null;
            Row row = null;
            for (Map.Entry<Category, Row> e : values.entrySet()) {
                if (row != null && row.max() >= e.getValue().max()) continue;
                cat = e.getKey();
                row = e.getValue();
            }
            if (row == null) break; // Nothing to place, apparently. Should never happen.

            double max = row.max();
            if (row.max() == -1) break; // We ran out of items to place, means we filled all slots

            int slot = row.maxIdx();

            // Default pick (no preferences), try to search for original position in kit.
            if (max == 0) {
                for (int i = 0; i < kit.size(); i++) {
                    if (cat == kit.get(i) && prediction[i] == null)
                        slot = i;
                }
            }

            if (prediction[slot] != null)
                throw new IllegalStateException("Tried to place two items in the same slot!");

            // If the slot is above 10, we intentionally simply do not set it as a prediction.
            // - If item is in hot bar, other items may take the spot. If no other item does, spot is kept.
            // - If item not in hot bar, it'll likely stay where it is.
            if (slot != Row.POCKETS_IDX) {
                prediction[slot] = cat;
                // Delete the "column" so nothing else tries to override this slot.
                for (Row r : values.values()) r.set(slot, -1);
            } else {
                // Inventory slots, remove 1 from the category. Set to -1 if below 0.
                double newVal = row.get(Row.POCKETS_IDX) - 1;
                row.set(Row.POCKETS_IDX, newVal < 0 ? -1 : newVal);
            }
            if (bag.remove(cat, 1) <= 1) values.remove(cat);
        }

        return CategorizedKit.of(prediction);
    }

    public Row predict(CategorizedKit kit, Category predict) {
        @Nullable Matrix mat = chances.get(predict);

        Row result = new Row();
        for (int i = 0; mat != null && i < kit.size(); i++) {
            if (kit.get(i) != predict) continue;

            Row row = mat.getRow(i);
            if (row.total() > 0) result.plusChances(row);

            if (i >= result.size()) break;
        }

        if (result.total() == 0) {
            // No preferences for this use-case, create dummy. Assume you WANT the default position(s).
            for (int i = 0; i < kit.size(); i++)
                if (kit.get(i) == predict) result.add(i, 1);
        }

        return result;
    }

    public void learn(CategorizedKit kit, CategorizedKit preference) {
        for (int i = 0; i < preference.size(); i++) {
            Category pi = preference.get(i);
            if (pi == null) continue;

            Matrix mat = chances.computeIfAbsent(pi, c -> new Matrix());
            if (kit.get(i) == pi) {
                mat.getRow(i).add(i, 1);
            } else {
                List<Integer> indexes = new ArrayList<>();
                for (int idx = 0; idx < kit.size(); idx++) {
                    Category it = kit.get(idx);
                    if (it == pi && it != preference.get(idx)) indexes.add(idx);
                }
                double weight = 1d / indexes.size();

                for (Integer idx : indexes) mat.getRow(idx).add(i, weight);
            }
        }
    }

}
