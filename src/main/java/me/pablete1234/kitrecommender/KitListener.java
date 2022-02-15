package me.pablete1234.kitrecommender;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.pablete1234.kitrecommender.engine.KitModifier;
import me.pablete1234.kitrecommender.engine.KitModifierFactory;
import me.pablete1234.kitrecommender.utils.ItemKitWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.pgm.api.PGM;
import tc.oc.pgm.api.match.event.MatchFinishEvent;
import tc.oc.pgm.api.player.MatchPlayer;
import tc.oc.pgm.kits.ApplyItemKitEvent;
import tc.oc.pgm.kits.ItemKit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generic KitModifier which acts as a proxy to player-specific kit modifiers
 * Instances of the player-specific kit modifiers are obtained from a {@link KitModifierFactory}
 */
public class KitListener implements Listener {

    private final KitModifierFactory factory;
    private final Map<ItemKit, ItemKitWrapper> sharedKitState = new HashMap<>();
    private final Table<UUID, ItemKit, KitModifier> playerKitModifiers = HashBasedTable.create();

    public KitListener(KitModifierFactory factory) {
        this.factory = factory;
    }

    @EventHandler
    public void adjustKit(ApplyItemKitEvent event) {
        if (!event.getPlayer().isParticipating()) return; // Ignore observer kits

        UUID player = event.getPlayer().getId();
        ItemKit kit = event.getKit();

        KitModifier km = playerKitModifiers.get(player, kit);
        if (km == null) playerKitModifiers.put(player, kit, km = factory.create(player, getKitWrapper(kit)));
        km.adjustKit(event);
    }

    @EventHandler
    public void learnPreferences(InventoryCloseEvent event) {
        MatchPlayer pl = PGM.get().getMatchManager().getPlayer(event.getPlayer());
        if (pl == null || !pl.isParticipating()) return; // Ignore observers

        UUID player = event.getPlayer().getUniqueId();
        playerKitModifiers.row(player).values().forEach(km -> km.learnPreferences(event));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerKitModifiers.row(event.getPlayer().getUniqueId()).clear();
    }

    @EventHandler
    public void onMatchFinish(MatchFinishEvent event) {
        sharedKitState.clear();
        playerKitModifiers.clear();
    }

    private ItemKitWrapper getKitWrapper(ItemKit kit) {
        return sharedKitState.computeIfAbsent(kit, ItemKitWrapper::new);
    }

}
