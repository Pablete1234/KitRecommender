package me.pablete1234.kit.util.model;

public class NoOpKitPredictor implements KitPredictor {
    @Override
    public CategorizedKit predictKit(CategorizedKit kit) {
        return kit;
    }

    @Override
    public void learn(CategorizedKit kit, CategorizedKit preference) {

    }
}
