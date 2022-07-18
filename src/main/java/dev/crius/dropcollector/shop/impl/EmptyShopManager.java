package dev.crius.dropcollector.shop.impl;

import dev.crius.dropcollector.shop.ShopManager;
import org.bukkit.inventory.ItemStack;

public class EmptyShopManager implements ShopManager {

    @Override
    public String getName() {
        return "Entity Based Shop Manager";
    }

    @Override
    public double getPrice(ItemStack itemStack, double def) {
        return def;
    }

}
