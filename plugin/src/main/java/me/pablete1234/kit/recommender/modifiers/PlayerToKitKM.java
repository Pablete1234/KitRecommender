package me.pablete1234.kit.recommender.modifiers;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import me.pablete1234.kit.recommender.itf.KitModifier;
import me.pablete1234.kit.recommender.util.KitUtils;
import me.pablete1234.kit.util.ItemKitWrapper;
import me.pablete1234.kit.util.KitSorter;
import me.pablete1234.kit.util.model.KitPredictor;
import me.pablete1234.kit.util.model.KitPredictor.CategorizedKit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import tc.oc.pgm.api.player.MatchPlayer;
import tc.oc.pgm.kits.ApplyItemKitEvent;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.kits.Slot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Player-specific KitModifier which acts as a proxy to player-and-kit-specific kit modifiers
 * Instances of the player-and-kit-specific kit modifiers are obtained from the provided factory
 */
public class PlayerToKitKM implements KitModifier {

    private final UUID player;
    private final KitPredictor predictor;

    private final Map<ItemKit, PlayerKitModel> kitModifiers = new HashMap<>();
    private final Set<PlayerKitModel> appliedKits = new ObjectArraySet<>(2);

    public PlayerToKitKM(UUID player, KitPredictor predictor) {
        this.player = player;
        this.predictor = predictor;
    }

    @Override
    public void adjustKit(ApplyItemKitEvent event) {
        PlayerKitModel km = kitModifiers.computeIfAbsent(event.getKit(), kit -> createModel(event));

        km.adjustKit(event);
        appliedKits.add(km);
    }

    @Override
    public boolean learnPreferences(InventoryCloseEvent event) {
        boolean learnt = false;
        for (KitModifier appliedKit : appliedKits)
            learnt |= appliedKit.learnPreferences(event);
        return learnt;
    }

    @Override
    public void cleanKits(UUID player) {
        if (!this.player.equals(player))
            throw new UnsupportedOperationException("Tried to cleanup a kit modifier for a different player.");
        appliedKits.clear();
    }

    @Override
    public void cleanup(UUID player) {
        if (!this.player.equals(player))
            throw new UnsupportedOperationException("Tried to cleanup a kit modifier for a different player.");
        cleanup();
    }

    @Override
    public void cleanup() {
        kitModifiers.values().forEach(pkm -> {
            predictor.learn(pkm.getKit().asCategorized(), CategorizedKit.of(pkm.getSlotItems()));
            pkm.cleanup();
        });
        kitModifiers.clear();
        appliedKits.clear();
    }

    private PlayerKitModel createModel(ApplyItemKitEvent event) {
        MatchPlayer player = event.getPlayer();

        ItemKitWrapper kit = ItemKitWrapper.of(event.getKit());
        PlayerKitModel model = new PlayerKitModel(this.player, kit);

        List<ItemStack> displacedItems = event.getDisplacedItems();
        List<PlayerKitModel> otherKits = KitUtils.findJointKits(appliedKits, displacedItems);

        // Simple (and most common) case, no other kits
        if (otherKits.isEmpty()) {
            CategorizedKit original = kit.asCategorized();
            CategorizedKit predicted = predictor.predictKit(original);

            // CategorizedKit is immutable, if they're equal it means no change needs to be predicted
            if (predicted != original)
                KitSorter.PREDICTOR.applyPreferences(predicted, kit, model.getSlotItems(), model.getFreeItems(), false);

            return model;
        }

        // Complicated use-case, multiple kits being given
        ItemKitWrapper condensedKit = KitUtils.condenseKits(otherKits, kit);

        CategorizedKit original = condensedKit.asCategorized();
        CategorizedKit predicted = predictor.predictKit(original);

        // Treat condensedKit as a model to put initial predictions on
        KitSorter.PREDICTOR.applyPreferences(predicted, condensedKit, condensedKit.getSlotItems(), condensedKit.getFreeItems(), false);


        // The group of joint kits has one in which player had an empty inventory.
        // We can safely clean their inventory, and re-apply the previous kits.
        if (otherKits.stream().anyMatch(PlayerKitModel::hadEmptyInventory)) {
            cleanupPreviousKits(player, otherKits, condensedKit, displacedItems);
        }

        // Treat condensedKit as if it was the players' inventory, and predict those movements on the other kits
        KitSorter.KIT.applyPreferences(condensedKit, model.getKit(), model.getSlotItems(), model.getFreeItems(), false);


        return model;
    }

    private void cleanupPreviousKits(MatchPlayer player,
                                     List<PlayerKitModel> otherKits,
                                     ItemKitWrapper predicted,
                                     List<ItemStack> displacedItems) {
        player.getInventory().clear();
        displacedItems.clear();

        for (PlayerKitModel model : otherKits) {
            model.getSlotItems().clear();
            model.getFreeItems().clear();
            model.getSlotItems().putAll(model.getKit().getSlotItems());
            model.getFreeItems().addAll(model.getKit().getFreeItems());

            KitSorter.KIT.applyPreferences(predicted, model.getKit(), model.getSlotItems(), model.getFreeItems(), false);

            model.getKit().getPGMKit().apply(player, model.wasForced(), displacedItems);
        }
    }

}
