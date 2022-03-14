package me.pablete1234.kitrecommender.modifiers;

import me.pablete1234.kitrecommender.itf.SimpleKitModifier;
import me.pablete1234.kitrecommender.utils.ItemKitWrapper;
import me.pablete1234.kitrecommender.utils.KitSorter;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.pgm.kits.ApplyItemKitEvent;
import tc.oc.pgm.kits.Slot;

import java.time.Duration;
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

    // Duration in which changes are always considered preferences, since application of the kit.
    // Changes made within this period are uncontested and always considered intended
    // For changes made outside the period, the replacement items are further checked.
    private static final Duration PREFERENCE_DURATION = Duration.ofSeconds(15);

    private final UUID player;
    private final ItemKitWrapper kit;

    private final Map<Slot, ItemStack> slotItems;
    private final List<ItemStack>      freeItems;

    private Instant considerPreferenceUntil = Instant.EPOCH;

    public PlayerKitModel(UUID player, ItemKitWrapper kit) {
        this.player = player;
        this.kit = kit;
        this.slotItems = new HashMap<>(kit.getSlotItems());
        this.freeItems = new ArrayList<>(kit.getFreeItems());
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
        considerPreferenceUntil = Instant.now().plus(PREFERENCE_DURATION);
    }

    @Override
    public boolean learnPreferences(InventoryCloseEvent event) {
        assert event.getPlayer().getUniqueId().equals(player);

        PlayerInventory inventory = event.getPlayer().getInventory();
        boolean lateEdit = Instant.now().isAfter(considerPreferenceUntil);

        KitSorter.PGM.learnPreferences(inventory, kit, slotItems, freeItems, lateEdit);
        return true;
    }

}
