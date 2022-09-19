package dev.crius.dropcollector.util;

import dev.crius.dropcollector.xseries.XMaterial;
import org.bukkit.Material;

public class ItemUtils {

    public static String getKey(Material material) {
        if (!XMaterial.supports(13)) return null;

        return (!material.isBlock() ? "item." : "block.") + "minecraft." + material.getKey().getKey();
    }

}
