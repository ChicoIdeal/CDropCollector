package dev.crius.dropcollector.entity.item;

import dev.crius.dropcollector.xseries.XMaterial;
import lombok.Data;

@Data
public class CItem {

    private final XMaterial material;
    private final double price;

}
