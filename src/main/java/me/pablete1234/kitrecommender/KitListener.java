package me.pablete1234.kitrecommender;

import me.pablete1234.kitrecommender.itf.KitModifier;
import me.pablete1234.kitrecommender.utils.ItemKitWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tc.oc.pgm.api.match.event.MatchFinishEvent;
import tc.oc.pgm.api.player.event.MatchPlayerDeathEvent;
import tc.oc.pgm.kits.ApplyItemKitEvent;
import tc.oc.pgm.kits.ApplyKitEvent;
import tc.oc.pgm.kits.ClearItemsKit;
import tc.oc.pgm.spawns.events.ParticipantSpawnEvent;

public class KitListener implements Listener {

    private final KitModifier kitModifier;

    public KitListener(KitModifier kitModifier) {
        this.kitModifier = kitModifier;
    }

    @EventHandler
    public void adjustKit(ApplyItemKitEvent event) {
        kitModifier.adjustKit(event);
    }

    @EventHandler
    public void learnPreferences(InventoryCloseEvent event) {
        kitModifier.learnPreferences(event);
    }

    @EventHandler
    public void onPlayerSpawn(ParticipantSpawnEvent event) {
        kitModifier.cleanKits(event.getPlayer().getId());
    }

    @EventHandler
    public void onPlayerDeath(MatchPlayerDeathEvent event) {
        kitModifier.cleanKits(event.getPlayer().getId());
    }

    @EventHandler
    public void onClearKit(ApplyKitEvent event) {
        if (event.getKit() instanceof ClearItemsKit && ((ClearItemsKit) event.getKit()).clearsItems())
            kitModifier.cleanKits(event.getPlayer().getId());
    }

    @EventHandler
    public void onMatchFinish(MatchFinishEvent event) {
        ItemKitWrapper.cleanup();
        kitModifier.cleanup();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        kitModifier.cleanup(event.getPlayer().getUniqueId());
    }

}
