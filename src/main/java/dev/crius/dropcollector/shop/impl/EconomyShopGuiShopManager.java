package dev.crius.dropcollector.shop.impl;

import dev.crius.dropcollector.shop.ShopManager;
import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import org.bukkit.inventory.ItemStack;

public class EconomyShopGuiShopManager implements ShopManager {

    @Override
    public String getName() {
        return "EconomyShopGUI";
    }

    @Override
    public double getPrice(ItemStack itemStack, double def) {
        Double sellPrice = EconomyShopGUIHook.getItemSellPrice(itemStack);
        return sellPrice == null ? def : sellPrice;
    }

}
