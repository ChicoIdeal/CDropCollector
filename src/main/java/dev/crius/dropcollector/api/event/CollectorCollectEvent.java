package dev.crius.dropcollector.api.event;

import dev.crius.dropcollector.collector.CollectedItem;
import dev.crius.dropcollector.collector.Collector;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CollectorCollectEvent extends CollectorEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CollectedItem item;
    private final int amount;

    public CollectorCollectEvent(Collector collector, CollectedItem item, int amount) {
        super(collector);
        this.amount = amount;
        this.item = item;
    }

    public CollectedItem getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
