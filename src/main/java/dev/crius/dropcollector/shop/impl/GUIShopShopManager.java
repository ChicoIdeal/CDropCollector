package dev.crius.dropcollector.shop.impl;

import com.pablo67340.guishop.api.GuiShopAPI;
import dev.crius.dropcollector.shop.ShopManager;
import org.bukkit.inventory.ItemStack;

public class GUIShopShopManager implements ShopManager {

    @Override
    public String getName() {
        return "GUIShop";
    }

    @Override
    public double getPrice(ItemStack itemStack, double def) {
        double sellPrice = GuiShopAPI.getSellPrice(itemStack, 1);
        return sellPrice == -1 ? def : sellPrice;
    }

}
