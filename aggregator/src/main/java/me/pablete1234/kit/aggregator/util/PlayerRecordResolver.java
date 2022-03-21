package me.pablete1234.kit.aggregator.util;

import me.pablete1234.kit.aggregator.exception.InvalidKitDataException;
import me.pablete1234.kit.util.InventoryImage;
import me.pablete1234.kit.util.KitSorter;
import me.pablete1234.kit.util.serialized.InventoryRecord;
import me.pablete1234.kit.util.serialized.KitPreferenceRecord;
import tc.oc.pgm.kits.Slot;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Performs the calculation of player references for an input list of InventoryRecords.
 *  - Searches the initial kit
 *  - Computes all subsequent player inventory changes
 *  - Determines a player's preferred kit layout
 *  - If not enough information is available, IgnoredFileException is thrown
 */
public class PlayerRecordResolver {

    private InventoryRecord kitRecord;
    private Map<Slot, Integer> items;
    private List<Integer> freeItems;

    private final KitBuffer originalKit = new KitBuffer();
    private final KitBuffer newKit = new KitBuffer();

    public KitPreferenceRecord resolve(Iterator<InventoryRecord> iterator) throws InvalidKitDataException {
        cleanup(); // Make sure we start in a clean state

        while (iterator.hasNext()) {
            InventoryRecord record = iterator.next();
            if (record.isAppliedKit()) {
                setupKitRecord(record);
                break;
            }
        }

        if (kitRecord == null)
            throw new InvalidKitDataException(InvalidKitDataException.Reason.NO_KIT_GIVEN);

        // Consider a kit with just 1 item too trivial to sort
        if (originalKit.emptySlots >= (InventoryImage.PLAYER_SIZE - 1))
            throw new InvalidKitDataException(InvalidKitDataException.Reason.SMALL_KIT_GIVEN);

        if (!iterator.hasNext())
            throw new InvalidKitDataException(InvalidKitDataException.Reason.TOO_FEW_RECORDS);

        long lastRecord = 0, lastGivenKit = kitRecord.getTimestamp();
        boolean edited = false;

        while (iterator.hasNext()) {
            InventoryRecord record = iterator.next();
            lastRecord = record.getTimestamp();
            if (record.isAppliedKit()) {
                lastGivenKit = lastRecord;

                newKit.set(record.getInventory());

                // Ignore the file if the new kit is missing some original items. Having more items is ok.
                if (!newKit.contains(originalKit))
                    throw new InvalidKitDataException(InvalidKitDataException.Reason.DIFFERING_KITS);
                continue;
            }
            edited = true;

            boolean lateEdit = lastRecord - lastGivenKit > KitSorter.PREFERENCE_DURATION.toMillis();
            KitSorter.IMAGE.learnPreferences(record.getInventory(), kitRecord.getInventory(), items, freeItems, lateEdit);
        }

        // Too few data, you barely were in that match
        Instant start = Instant.ofEpochMilli(kitRecord.getTimestamp());
        Instant end = Instant.ofEpochMilli(lastRecord);
        int timeframe = (int) start.until(end, ChronoUnit.SECONDS);

        InventoryImage kit = kitRecord.getInventory();
        InventoryImage preferences = resultInventory();

        cleanup();
        return new KitPreferenceRecord(timeframe, edited, kit, preferences);
    }

    private void setupKitRecord(InventoryRecord kitRecord) throws InvalidKitDataException {
        this.kitRecord = kitRecord;
        this.items = new HashMap<>();
        for (int i = 0; i < InventoryImage.PLAYER_SIZE; i++) {
            int item = kitRecord.getInventory().getItem(i);
            if (item != 0) {
                items.put(Slot.Player.forIndex(i), item);
                if (InventoryImage.getMaterial(item) == null)
                    throw new InvalidKitDataException(InvalidKitDataException.Reason.CORRUPTED_DATA);
            }
        }
        this.freeItems = new ArrayList<>();
        this.originalKit.set(kitRecord.getInventory());
    }

    private void cleanup() {
        this.kitRecord = null;
        this.items = null;
        this.freeItems = null;
    }

    private InventoryImage resultInventory() {
        int[] contents = new int[InventoryImage.PLAYER_SIZE];
        // Place the known items
        for (Map.Entry<Slot, Integer> entry : items.entrySet()) {
            contents[entry.getKey().getIndex()] = entry.getValue();
        }
        // Place remaining items anywhere that is empty
        for (Integer item : freeItems) {
            for (int slot = 0; slot < InventoryImage.PLAYER_SIZE; slot++) {
                if (contents[slot] == 0) {
                    contents[slot] = item;
                    break;
                }
            }
        }
        return new InventoryImage(contents);
    }

    private static class KitBuffer {
        private final int[] items = new int[InventoryImage.PLAYER_SIZE];
        private int emptySlots;

        private void set(InventoryImage image) {
            image.copyInto(items);
            for (int i = 0; i < items.length; i++)
                items[i] = items[i] & ~InventoryImage.AMOUNT_MASK;

            Arrays.sort(items);
            this.emptySlots = 0;
            while (this.items[this.emptySlots] == 0) this.emptySlots++;
        }

        private boolean contains(KitBuffer other) {
            int othIdx = other.emptySlots, thisIdx = emptySlots;
            // If at any point our index becomes bigger than other index, we know
            // we for sure do not contain all the items in the other buffer.
            // eg: other index = 30, this index = 35, we have just 1 more item, but there are 6 remaining to be found.
            if (othIdx < thisIdx) return false;

            for (; othIdx < other.items.length; thisIdx++, othIdx++) {
                while (items[thisIdx] != other.items[othIdx]) { // While items don't match, keep going to next item
                    thisIdx++;
                    if (othIdx < thisIdx) return false;
                }
            }
            return true;
        }
    }


}
