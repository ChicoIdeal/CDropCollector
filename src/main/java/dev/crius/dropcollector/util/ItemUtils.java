package dev.crius.dropcollector.util;

import dev.crius.dropcollector.xseries.XMaterial;
import org.bukkit.inventory.ItemStack;

public class ItemUtils {

    public static String getLocalizedName(ItemStack itemStack) {
        if (itemStack.getItemMeta() == null) return itemStack.getType().name();
        if (XMaterial.supports(12) && !itemStack.getItemMeta().hasLocalizedName()) return itemStack.getType().name();

        return XMaterial.supports(12) ? itemStack.getItemMeta().getLocalizedName() : itemStack.getType().name();
    }

}
