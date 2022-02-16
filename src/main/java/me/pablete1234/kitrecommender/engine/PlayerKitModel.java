package me.pablete1234.kitrecommender.engine;

import me.pablete1234.kitrecommender.utils.ItemKitWrapper;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import tc.oc.pgm.kits.ApplyItemKitEvent;
import tc.oc.pgm.kits.Slot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents the kit, modified to fit what a specific player wants.
 */
public class PlayerKitModel implements KitModifier {

    private final UUID player;
    private final ItemKitWrapper kit;

    private final Map<Slot, ItemStack> slotItems;
    private final List<ItemStack>      freeItems;

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
    }

    @Override
    public void learnPreferences(InventoryCloseEvent event) {
        assert event.getPlayer().getUniqueId().equals(player);

        PlayerInventory inventory = event.getPlayer().getInventory();

        Map<Slot, ItemStack> potentiallyUnmapped = new HashMap<>();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            Slot slot = Slot.Player.forIndex(i);
            ItemStack current = slotItems.get(slot);

            // No item, or item is not part of the kit. This is further checked later.
            if (item == null || !kit.maybeContains(item)) {
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

        Iterator<ItemStack> itemIt = potentiallyUnmapped.values().iterator();
        while (itemIt.hasNext()) {
            ItemStack kitItem = itemIt.next();
            if (!areSimilar(kitItem, search)) continue;
            itemIt.remove();
            return kitItem;
        }

        itemIt = freeItems.iterator();
        while (itemIt.hasNext()) {
            ItemStack kitItem = itemIt.next();
            if (!areSimilar(kitItem, search)) continue;
            itemIt.remove();
            return kitItem;
        }
        return null;
    }

    private boolean areSimilar(ItemStack kit, ItemStack item) {
        return item != null &&
                kit.getType() == item.getType() &&
                // If material has durability (eg: sword), ignore. Otherwise (eg: wool color) check durability.
                (kit.getType().getMaxDurability() > 0 || kit.getDurability() == item.getDurability()) &&
                kit.getEnchantments().equals(item.getEnchantments());
    }
}
