package dev.crius.dropcollector.collector;

import dev.crius.dropcollector.entity.item.CItem;
import lombok.Data;

@Data
public class CollectedItem {

    private final CItem item;
    private int amount;

}
