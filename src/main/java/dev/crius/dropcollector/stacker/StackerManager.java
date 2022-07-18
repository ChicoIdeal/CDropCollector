package dev.crius.dropcollector.stacker;

import dev.crius.dropcollector.hook.Hook;
import org.bukkit.entity.Item;

public interface StackerManager extends Hook {

    int getRealAmount(Item item);

    void setAmount(Item item, int amount);

}
