package dev.crius.dropcollector.shop.impl;

import dev.crius.dropcollector.shop.ShopManager;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.inventory.ItemStack;

public class ShopGuiPlusShopManager implements ShopManager {

    @Override
    public String getName() {
        return "ShopGUIPlus";
    }

    @Override
    public double getPrice(ItemStack itemStack, double def) {
        double price = ShopGuiPlusApi.getItemStackPriceSell(itemStack);
        return price == -1 ? def : price;
    }
}
