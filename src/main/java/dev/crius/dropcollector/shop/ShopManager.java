package dev.crius.dropcollector.shop;

import dev.crius.dropcollector.hook.Hook;
import org.bukkit.inventory.ItemStack;

public interface ShopManager extends Hook {

    double getPrice(ItemStack itemStack, double def);

}
