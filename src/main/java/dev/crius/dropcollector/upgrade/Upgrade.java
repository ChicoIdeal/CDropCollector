package dev.crius.dropcollector.upgrade;

import lombok.Data;

@Data
public class Upgrade {

    private final String displayName;
    private final double price;
    private final int max;
    private final int place;

}
