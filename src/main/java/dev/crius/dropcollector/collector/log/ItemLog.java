package dev.crius.dropcollector.collector.log;

import dev.crius.dropcollector.xseries.XMaterial;
import lombok.Data;

@Data
public class ItemLog {

    private final LogType type;
    /**
     * "ALL" if action contains all items in the
     */
    private final String material;
    private final int amount;
    private final String player;

}
