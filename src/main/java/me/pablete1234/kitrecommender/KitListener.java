package me.pablete1234.kitrecommender;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
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
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent;
import tc.oc.pgm.kits.ApplyItemKitEvent;
import tc.oc.pgm.kits.ApplyKitEvent;
import tc.oc.pgm.kits.ClearItemsKit;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.spawns.events.ParticipantSpawnEvent;

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
    private final SetMultimap<UUID, ItemKit> appliedKits = HashMultimap.create();

    public KitListener(KitModifierFactory factory) {
        this.factory = factory;
    }

    @EventHandler
    public void adjustKit(ApplyItemKitEvent event) {
        if (!event.getPlayer().canInteract()) return; // Ignore observer kits

        UUID player = event.getPlayer().getId();
        ItemKit kit = event.getKit();

        KitModifier km = playerKitModifiers.get(player, kit);
        if (km == null) playerKitModifiers.put(player, kit, km = factory.create(player, getKitWrapper(kit)));
        km.adjustKit(event);
        appliedKits.put(player, kit);
    }

    @EventHandler
    public void learnPreferences(InventoryCloseEvent event) {
        MatchPlayer pl = PGM.get().getMatchManager().getPlayer(event.getPlayer());
        if (pl == null || !pl.canInteract()) return; // Ignore observers

        UUID player = event.getPlayer().getUniqueId();
        appliedKits.get(player).forEach(kit -> {
            KitModifier km = playerKitModifiers.get(player, kit);
            // KitModifier should never be null, since appliedKits.put is only AFTER adjustKit is called,
            // but we rather be safe than sorry
            if (km != null) km.learnPreferences(event);
        });
    }

    @EventHandler
    public void onPlayerSpawn(ParticipantSpawnEvent event) {
        appliedKits.get(event.getPlayer().getId()).clear();
    }

    @EventHandler
    public void onPlayerDeath(MatchPlayerDeathEvent event) {
        appliedKits.get(event.getPlayer().getId()).clear();
    }

    @EventHandler
    public void onClearKit(ApplyKitEvent event) {
        if (!(event.getKit() instanceof ClearItemsKit)) return;
        appliedKits.removeAll(event.getPlayer().getId());
    }

    @EventHandler
    public void onMatchFinish(MatchFinishEvent event) {
        sharedKitState.clear();
        playerKitModifiers.clear();
        appliedKits.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID player = event.getPlayer().getUniqueId();
        playerKitModifiers.row(player).clear();
        appliedKits.removeAll(player);
    }

    private ItemKitWrapper getKitWrapper(ItemKit kit) {
        return sharedKitState.computeIfAbsent(kit, ItemKitWrapper::new);
    }

}
