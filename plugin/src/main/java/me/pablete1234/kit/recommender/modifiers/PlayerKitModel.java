package me.pablete1234.kit.recommender.modifiers;

import me.pablete1234.kit.recommender.itf.SimpleKitModifier;
import me.pablete1234.kit.recommender.util.KitUtils;
import me.pablete1234.kit.util.ItemKitWrapper;
import me.pablete1234.kit.util.KitSorter;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;
import tc.oc.pgm.kits.ApplyItemKitEvent;
import tc.oc.pgm.kits.Slot;

import java.lang.ref.WeakReference;
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
    private final ItemKitWrapper.PGM kit;

    private final Map<Slot, ItemStack> slotItems;
    private final List<ItemStack>      freeItems;

    // Data about last time the kit was given
    private boolean wasForced; // Was it forced
    private boolean hadEmptyInventory; // Did the player have empty inv before giving?
    private WeakReference<List<ItemStack>> lastDisplacedItems; // The list of items

    private Instant considerPreferenceUntil = Instant.EPOCH;

    public PlayerKitModel(UUID player, ItemKitWrapper.PGM kit) {
        this.player = player;
        this.kit = kit;
        this.slotItems = new HashMap<>(kit.getSlotItems());
        this.freeItems = new ArrayList<>(kit.getFreeItems());
    }

    public ItemKitWrapper.PGM getKit() {
        return kit;
    }

    public Map<Slot, ItemStack> getSlotItems() {
        return slotItems;
    }

    public List<ItemStack> getFreeItems() {
        return freeItems;
    }

    public boolean wasForced() {
        return wasForced;
    }

    public boolean hadEmptyInventory() {
        return hadEmptyInventory;
    }

    public @Nullable List<ItemStack> getLastDisplacedItems() {
        return lastDisplacedItems.get();
    }

    @Override
    public void adjustKit(ApplyItemKitEvent event) {
        assert event.getPlayer().getId().equals(player);
        assert event.getKit() == kit.getPGMKit();

        // Update data about last applied
        this.wasForced = event.isForce();
        this.lastDisplacedItems = new WeakReference<>(event.getDisplacedItems());
        this.hadEmptyInventory = event.getDisplacedItems().isEmpty() &&
                KitUtils.isEmpty(event.getPlayer().getInventory());

        // Completely override whatever kit was going to be given, with the kit customized for the player
        // We must use item clones, as pgm can modify them when giving to the player (eg: subtract from amount)
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

}
