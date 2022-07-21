package me.pablete1234.kit.util.model;

import com.google.common.collect.Multiset;
import me.pablete1234.kit.util.category.Category;
import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.kits.Slot;

import java.util.Map;

/**
 * Predictor for kits, takes in a kit and says how this user will want it sorted,
 * as well as learning and adapting based on seeing what the user has done with their kit.
 */
public interface KitPredictor {

    /**
     * Predicts the output kit given an input kit.
     * @param kit input kit
     * @return input kit if no modification is to be done, otherwise a new kit with the result.
     */
    CategorizedKit predictKit(CategorizedKit kit);

    /**
     * Updates the prediction model to learn from what the player has done with their kit during a match.
     * @param kit the original kit given to the player
     * @param preference the final kit preference for the player at the end of the match
     */
    void learn(CategorizedKit kit, CategorizedKit preference);

    /**
     * Immutable kit-of-categories representation
     * Represents an inventory of a player with the 36 categories, one per inventory slot (or null if empty).
     */
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
