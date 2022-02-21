package me.pablete1234.kitrecommender.modifiers;

import me.pablete1234.kitrecommender.itf.KitModifier;
import org.bukkit.event.inventory.InventoryCloseEvent;
import tc.oc.pgm.api.PGM;
import tc.oc.pgm.api.player.MatchPlayer;
import tc.oc.pgm.kits.ApplyItemKitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Generic KitModifier which acts as a proxy to player-specific kit modifiers
 * Instances of the player-specific kit modifiers are obtained from the provided factory
 */
public class GlobalToPlayerKM implements KitModifier {

    private final Function<UUID, KitModifier> factory;
    private final Map<UUID, KitModifier> playerKitModifiers = new HashMap<>();

    public GlobalToPlayerKM(Function<UUID, KitModifier> factory) {
        this.factory = factory;
    }

    @Override
    public void adjustKit(ApplyItemKitEvent event) {
        if (!event.getPlayer().canInteract()) return; // Ignore observer kits

        UUID player = event.getPlayer().getId();
        KitModifier km = playerKitModifiers.get(player);
        if (km == null) playerKitModifiers.put(player, km = factory.apply(player));
        km.adjustKit(event);
    }

    @Override
    public void learnPreferences(InventoryCloseEvent event) {
        MatchPlayer pl = PGM.get().getMatchManager().getPlayer(event.getPlayer());
        if (pl == null || !pl.canInteract()) return; // Ignore observers

        UUID player = event.getPlayer().getUniqueId();
        KitModifier km = playerKitModifiers.get(player);
        if (km != null) km.learnPreferences(event);
    }

    @Override
    public void cleanKits(UUID player) {
        KitModifier km = playerKitModifiers.get(player);
        if (km != null) km.cleanKits(player);
    }

    @Override
    public void cleanup(UUID player) {
        KitModifier km = playerKitModifiers.remove(player);
        if (km != null) km.cleanup(player);
    }

    @Override
    public void cleanup() {
        playerKitModifiers.values().forEach(KitModifier::cleanup);
        playerKitModifiers.clear();
    }
}
