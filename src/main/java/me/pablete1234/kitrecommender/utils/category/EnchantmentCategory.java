package me.pablete1234.kitrecommender.utils.category;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public interface EnchantmentCategory extends Category {

    default byte getData(ItemStack is) {
        Enchantment[] enchants = getEnchantments();
        byte data = 0;
        for (int i = 0; i < enchants.length; i++)
            if (is.containsEnchantment(enchants[i])) data |= (1 << i);
        return data;
    }

    default void putData(ItemStack is, byte data) {
        Enchantment[] enchants = getEnchantments();
        for (int i = 0; i < enchants.length; i++)
            if ((data & (1 << i)) != 0) is.addUnsafeEnchantment(enchants[i], 1);
    }

    Enchantment[] getEnchantments();
}
