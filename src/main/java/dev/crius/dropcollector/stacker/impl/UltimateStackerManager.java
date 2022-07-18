package dev.crius.dropcollector.stacker.impl;

import com.songoda.ultimatestacker.UltimateStacker;
import dev.crius.dropcollector.stacker.StackerManager;
import org.bukkit.entity.Item;

public class UltimateStackerManager implements StackerManager {

    @Override
    public String getName() {
        return "UltimateStacker";
    }

    @Override
    public int getRealAmount(Item item) {
        return UltimateStacker.getActualItemAmount(item);
    }

    @Override
    public void setAmount(Item item, int amount) {
        UltimateStacker.updateItemAmount(item, amount);
    }

}
