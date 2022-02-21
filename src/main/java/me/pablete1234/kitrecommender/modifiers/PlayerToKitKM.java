package me.pablete1234.kitrecommender.modifiers;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import me.pablete1234.kitrecommender.itf.KitModifier;
import me.pablete1234.kitrecommender.utils.ItemKitWrapper;
import org.bukkit.event.inventory.InventoryCloseEvent;
import tc.oc.pgm.kits.ApplyItemKitEvent;
import tc.oc.pgm.kits.ItemKit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Player-specific KitModifier which acts as a proxy to player-and-kit-specific kit modifiers
 * Instances of the player-and-kit-specific kit modifiers are obtained from the provided factory
 */
public class PlayerToKitKM implements KitModifier {

    private final UUID player;
    private final BiFunction<UUID, ItemKitWrapper, KitModifier> factory;

    private final Map<ItemKit, KitModifier> kitModifiers = new HashMap<>();
    private final Set<KitModifier> appliedKits = new ObjectArraySet<>(2);

    public PlayerToKitKM(UUID player, BiFunction<UUID, ItemKitWrapper, KitModifier> factory) {
        this.player = player;
        this.factory = factory;
    }

    @Override
    public void adjustKit(ApplyItemKitEvent event) {
        KitModifier km = kitModifiers.get(event.getKit());
        if (km == null) kitModifiers.put(event.getKit(), km = factory.apply(player, ItemKitWrapper.of(event.getKit())));
        km.adjustKit(event);
        appliedKits.add(km);
    }

    @Override
    public void learnPreferences(InventoryCloseEvent event) {
        for (KitModifier appliedKit : appliedKits)
            appliedKit.learnPreferences(event);
    }

    @Override
    public void cleanKits(UUID player) {
        appliedKits.clear();
    }

    @Override
    public void cleanup(UUID player) {
        if (!this.player.equals(player))
            throw new UnsupportedOperationException("Tried to cleanup a kit modifier for a different player.");
        kitModifiers.clear();
        appliedKits.clear();
    }

    @Override
    public void cleanup() {
        kitModifiers.clear();
        appliedKits.clear();
    }
}
