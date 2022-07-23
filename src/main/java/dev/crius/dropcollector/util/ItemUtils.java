package dev.crius.dropcollector.util;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.xseries.XMaterial;
import org.bukkit.Material;

public class ItemUtils {

    public static String getKey(Material material) {
        if (!XMaterial.supports(12)) return null;

        String key = (material.isItem() ? "item." : "block.") + "minecraft." + material.getKey().getKey();
        DropCollectorPlugin.getInstance().debug(key);
        return key;
    }

}
