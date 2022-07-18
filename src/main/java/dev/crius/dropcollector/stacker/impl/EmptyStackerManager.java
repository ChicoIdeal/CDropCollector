package dev.crius.dropcollector.stacker.impl;

import dev.crius.dropcollector.stacker.StackerManager;
import org.bukkit.entity.Item;

public class EmptyStackerManager implements StackerManager {

    @Override
    public String getName() {
        return "Empty Stacker Manager";
    }

    @Override
    public int getRealAmount(Item item) {
        return item.getItemStack().getAmount();
    }

    @Override
    public void setAmount(Item item, int amount) {
        item.getItemStack().setAmount(amount);
    }

}
