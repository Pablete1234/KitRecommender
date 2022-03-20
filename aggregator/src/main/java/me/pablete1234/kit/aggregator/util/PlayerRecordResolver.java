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
    private final int[] kitInvItems = new int[InventoryImage.PLAYER_SIZE];
    private final int[] bufferItems = new int[InventoryImage.PLAYER_SIZE];

    public KitPreferenceRecord resolve(Iterator<InventoryRecord> iterator) throws InvalidKitDataException {
        cleanup(); // Make sure we start in a clean state
        if (!iterator.hasNext())
            throw new InvalidKitDataException(InvalidKitDataException.Reason.FEW_RECORDS);

        while (iterator.hasNext()) {
            InventoryRecord record = iterator.next();
            if (record.isAppliedKit()) {
                setupKitRecord(record);
                break;
            }
        }

        // Consider a kit with just 1 or 2 items too trivial to sort
        if (kitRecord == null || Arrays.stream(kitInvItems).filter(i -> i != 0).count() <= 2)
            throw new InvalidKitDataException(InvalidKitDataException.Reason.NO_KIT_GIVEN);

        long lastRecord = 0, lastGivenKit = kitRecord.getTimestamp();

        while (iterator.hasNext()) {
            InventoryRecord record = iterator.next();
            lastRecord = record.getTimestamp();
            if (record.isAppliedKit()) {
                lastGivenKit = record.getTimestamp();

                copySortedWithoutAmount(record.getInventory(), bufferItems);

                kitLoop:
                for (int kitInvItem : kitInvItems) {
                    for (int bufferItem : bufferItems) {
                        if (kitInvItem == bufferItem) continue kitLoop;
                    }

                    System.out.println(kitRecord.getInventory().toString());
                    System.out.println(record.getInventory().toString());
                    throw new InvalidKitDataException(InvalidKitDataException.Reason.DIFFERING_KITS);
                }
                continue;
            }

            boolean lateEdit = lastRecord - lastGivenKit > KitSorter.PREFERENCE_DURATION.toMillis();
            KitSorter.IMAGE.learnPreferences(record.getInventory(), kitRecord.getInventory(), items, freeItems, lateEdit);
        }

        // Too few data, you barely were in that match
        Instant start = Instant.ofEpochMilli(kitRecord.getTimestamp());
        Instant end = Instant.ofEpochMilli(lastRecord);
        if (start.until(end, ChronoUnit.SECONDS) < 30)
            throw new InvalidKitDataException(InvalidKitDataException.Reason.SMALL_TIMEFRAME);

        InventoryImage kit = kitRecord.getInventory();
        InventoryImage preferences = resultInventory();

        cleanup();
        return new KitPreferenceRecord(kit, preferences);
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
        copySortedWithoutAmount(kitRecord.getInventory(), this.kitInvItems);
    }

    private void copySortedWithoutAmount(InventoryImage image, int[] items) {
        image.copyInto(items);
        Arrays.sort(items);
        for (int i = 0; i < items.length; i++) {
            items[i] = items[i] & ~InventoryImage.AMOUNT_MASK;
        }
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


}
