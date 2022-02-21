package me.pablete1234.kitrecommender.modifiers;

import me.pablete1234.kitrecommender.itf.SimpleKitModifier;
import me.pablete1234.kitrecommender.utils.Categories;
import me.pablete1234.kitrecommender.utils.InventoryImage;
import me.pablete1234.kitrecommender.utils.ItemKitWrapper;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.pgm.kits.ApplyItemKitEvent;
import tc.oc.pgm.kits.Slot;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    public void learnPreferences(InventoryCloseEvent event) {
        assert event.getPlayer().getUniqueId().equals(player);

        PlayerInventory inventory = event.getPlayer().getInventory();

        Map<Slot, ItemStack> potentiallyUnmapped = new HashMap<>();

        for (int i = 0; i < InventoryImage.PLAYER_SIZE; i++) {
            ItemStack item = inventory.getItem(i);
            Slot slot = Slot.Player.forIndex(i);
            ItemStack current = slotItems.get(slot);

            // No item, or item is not part of the kit. This is further checked later.
            if (item == null || kit.doesntContain(item)) {
                // An item in the kit exists for that slot, pollItem would not be able to find it if we
                // just leave it here.
                // The solution is to move the item to potentiallyUnmapped, and if nothing claims it, it will
                // end up back to slotItems at the end.
                if (current != null) {
                    potentiallyUnmapped.put(slot, current);
                    slotItems.remove(slot);
                }
                continue;
            }

            // Item is already in the desired slot
            if (areSimilar(item, current)) continue;

            // Search, and pick-out the corresponding kit item
            ItemStack kitItem = pollItem(inventory, slot, item, potentiallyUnmapped);
            // Apparently the item isn't in the forward part of the kit, it may already have been mapped
            // and there are multiple items of the same type in the kit.
            if (kitItem == null) {
                // The same way as before, because the slot does not map to the kit, we need to move the item
                // to potentiallyUnmapped, and if nothing claims it, it will end up back to slotItems at the end.
                if (current != null) {
                    potentiallyUnmapped.put(slot, current);
                    slotItems.remove(slot);
                }
                continue;
            }

            // Displace the current item, a later iteration will put it in another slot... if necessary
            if (current != null) freeItems.add(current);
            slotItems.put(slot, kitItem);
        }
        potentiallyUnmapped.forEach((slot, item) -> {
            if (slotItems.containsKey(slot)) freeItems.add(item);
            else slotItems.put(slot, item);
        });
    }

    private ItemStack pollItem(PlayerInventory inventory,
                               Slot currentSlot,
                               ItemStack search,
                               Map<Slot, ItemStack> potentiallyUnmapped) {

        Iterator<Map.Entry<Slot, ItemStack>> slotIt = slotItems.entrySet().iterator();
        while (slotIt.hasNext()) {
            Map.Entry<Slot, ItemStack> entry = slotIt.next();
            if (entry.getKey().getIndex() < currentSlot.getIndex()) continue;

            ItemStack kitItem = entry.getValue();
            // Ignore this match if an item also exists in the designated slot in the kit
            // This is to prevent moving an item that shouldn't need to be moved.
            if (!areSimilar(kitItem, search) ||
                    areSimilar(kitItem, inventory.getItem(entry.getKey().getIndex()))) continue;
            slotIt.remove();
            return kitItem;
        }

        boolean timedOut = Instant.now().isAfter(considerPreferenceUntil);
        slotIt = potentiallyUnmapped.entrySet().iterator();
        while (slotIt.hasNext()) {
            Map.Entry<Slot, ItemStack> entry = slotIt.next();
            ItemStack kitItem = entry.getValue();
            if (!areSimilar(kitItem, search) ||
                    // Avoid moving an item from hot bar to pockets, if there's a loosely valid replacement in place
                    (entry.getKey() instanceof Slot.Player.Hotbar && currentSlot instanceof Slot.Player.Pockets &&
                            isReplacement(kitItem, inventory.getItem(entry.getKey().getIndex()), timedOut))) continue;

            slotIt.remove();
            return kitItem;
        }

        Iterator<ItemStack> itemIt = freeItems.iterator();
        while (itemIt.hasNext()) {
            ItemStack kitItem = itemIt.next();
            if (!areSimilar(kitItem, search)) continue;
            itemIt.remove();
            return kitItem;
        }
        return null;
    }

    /**
     * Checks if two items are similar. Similar means they are the same type of item,
     * but allow different quantity or durability.
     * @param kit The item in the kit
     * @param item The item in the player inventory
     * @return true if the items are similar, false otherwise
     */
    private boolean areSimilar(ItemStack kit, ItemStack item) {
        return item != null &&
                kit.getType() == item.getType() &&
                // If material has durability (eg: sword), ignore. Otherwise (eg: wool color) check durability.
                (kit.getType().getMaxDurability() > 0 || kit.getDurability() == item.getDurability()) &&
                kit.getEnchantments().equals(item.getEnchantments());
    }

    /**
     * Check if an item is being replaced by a "better" item (versus being moved for preference reasons).
     * This is defined as an upgrade or replacement.
     * Examples:
     *  - Diamond sword - Iron sword
     *  - Diamond helmet - Iron helmet
     *  - Bow with different enchantments
     *  - Water bucket - Empty bucket
     *  - Random material not in the kit, is a replacement or not based on {@param lateEdit}
     *
     * @param kit The item in the kit
     * @param item The item in the player inventory
     * @param lateEdit if the edit is not right after the kit is given
     * @return true if {@param item} is probably a replacement for {@param kit},
     *         false if we think this is a player preference difference.
     */
    private boolean isReplacement(ItemStack kit, ItemStack item, boolean lateEdit) {
        if (item == null) return false;
        Material m1 = kit.getType(), m2 = item.getType();

        // If the material is the same, it's a good enough replacement. (eg: bow)
        if (m1 == m2) return true;
        // Same category (eg: diamond sword replacing iron sword, or filled bucket replacing empty bucket)
        if (Categories.equal(kit.getType(), item.getType())) return true;

        return lateEdit && this.kit.doesntContain(item);
    }

}
