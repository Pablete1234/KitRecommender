package me.pablete1234.kit.recommender.itf;

import me.pablete1234.kit.util.ItemKitWrapper;
import me.pablete1234.kit.util.model.KitPredictor;

import java.util.UUID;

@FunctionalInterface
public interface PlayerKitFactory {

    KitModifier apply(UUID player, ItemKitWrapper kit, KitPredictor predictor);

}
