package me.pablete1234.kitrecommender.modifiers;

import me.pablete1234.kitrecommender.itf.KitModifier;
import org.bukkit.event.inventory.InventoryCloseEvent;
import tc.oc.pgm.kits.ApplyItemKitEvent;

import java.util.UUID;

public class DataCollectorKM implements KitModifier {

    private final UUID player;
    private final KitModifier downstream;

    public DataCollectorKM(UUID player, KitModifier downstream) {
        this.player = player;
        this.downstream = downstream;
    }

    @Override
    public void adjustKit(ApplyItemKitEvent event) {
        this.downstream.adjustKit(event);
    }

    @Override
    public void learnPreferences(InventoryCloseEvent event) {
        this.downstream.learnPreferences(event);
    }

    @Override
    public void cleanKits(UUID player) {
        this.downstream.cleanKits(player);
    }

    @Override
    public void cleanup(UUID player) {
        this.downstream.cleanup(player);
    }

    @Override
    public void cleanup() {
        this.downstream.cleanup();
    }
}
