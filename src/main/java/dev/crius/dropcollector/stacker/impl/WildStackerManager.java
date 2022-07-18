package dev.crius.dropcollector.stacker.impl;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import dev.crius.dropcollector.stacker.StackerManager;
import org.bukkit.entity.Item;

public class WildStackerManager implements StackerManager {

    @Override
    public String getName() {
        return "WildStacker";
    }

    @Override
    public int getRealAmount(Item item) {
        return WildStackerAPI.getItemAmount(item);
    }

    @Override
    public void setAmount(Item item, int amount) {
        WildStackerAPI.getStackedItem(item).setStackAmount(amount, true);
    }

}
