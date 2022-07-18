package dev.crius.dropcollector.shop.impl;

import dev.crius.dropcollector.shop.ShopManager;
import lee.code.onestopshop.OneStopShop;
import org.bukkit.inventory.ItemStack;

public class OneStopShopShopManager implements ShopManager {

    @Override
    public String getName() {
        return "OneStopShop";
    }

    @Override
    public double getPrice(ItemStack itemStack, double def) {
        double price = OneStopShop.getPlugin().getApi().getItemSellValue(itemStack);
        return price == -1 ? def : price;
    }

}
