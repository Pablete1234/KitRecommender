package me.pablete1234.kit.recommender.modifiers;

import me.pablete1234.kit.recommender.itf.SimpleKitModifier;
import me.pablete1234.kit.util.ItemKitWrapper;
import me.pablete1234.kit.util.KitSorter;
import me.pablete1234.kit.util.model.KitPredictor;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.pgm.kits.ApplyItemKitEvent;
import tc.oc.pgm.kits.Slot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents the kit, modified to fit what a specific player wants.
 */
public class PlayerKitModel implements SimpleKitModifier {

    private final UUID player;
    private final ItemKitWrapper kit;

    private final Map<Slot, ItemStack> slotItems;
    private final List<ItemStack>      freeItems;

    private final KitPredictor predictor;

    private Instant considerPreferenceUntil = Instant.EPOCH;

    public PlayerKitModel(UUID player, ItemKitWrapper kit, KitPredictor predictor) {
        this.player = player;
        this.kit = kit;
        this.predictor = predictor;
        this.slotItems = new HashMap<>(kit.getSlotItems());
        this.freeItems = new ArrayList<>(kit.getFreeItems());

        predictor.predictKit(kit, slotItems, freeItems);
    }

    @Override
    public void adjustKit(ApplyItemKitEvent event) {
        assert event.getPlayer().getId().equals(player);
        assert event.getKit() == kit.getKit();

        // Completely override whatever kit was going to be given, with the kit customized for the player
        // We must use item clones, as pgm can modify them when giving to the player
        event.getSlotItems().clear();
        slotItems.forEach((slot, item) -> event.getSlotItems().put(slot, item.clone()));
        event.getFreeItems().clear();
        freeItems.forEach(item -> event.getFreeItems().add(item.clone()));
        considerPreferenceUntil = Instant.now().plus(KitSorter.PREFERENCE_DURATION);
    }

    @Override
    public boolean learnPreferences(InventoryCloseEvent event) {
        assert event.getPlayer().getUniqueId().equals(player);

        PlayerInventory inventory = event.getPlayer().getInventory();
        boolean lateEdit = Instant.now().isAfter(considerPreferenceUntil);

        KitSorter.PGM.applyPreferences(inventory, kit, slotItems, freeItems, lateEdit);
        return true;
    }

    @Override
    public void cleanup() {
        predictor.learn(kit, slotItems, freeItems);
    }
}
