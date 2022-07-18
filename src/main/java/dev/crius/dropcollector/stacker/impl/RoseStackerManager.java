package dev.crius.dropcollector.stacker.impl;

import dev.crius.dropcollector.stacker.StackerManager;
import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedItem;
import org.bukkit.entity.Item;

public class RoseStackerManager implements StackerManager {

    @Override
    public String getName() {
        return "RoseStacker";
    }

    @Override
    public int getRealAmount(Item item) {
        StackedItem stackedItem = RoseStackerAPI.getInstance().getStackedItem(item);
        if (stackedItem == null) return 0;

        return stackedItem.getStackSize();
    }

    @Override
    public void setAmount(Item item, int amount) {
        StackedItem stackedItem = RoseStackerAPI.getInstance().getStackedItem(item);
        if (stackedItem == null) return;

        stackedItem.setStackSize(amount);
    }

}
