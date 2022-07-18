package dev.crius.dropcollector.api.event;

import dev.crius.dropcollector.collector.CollectedItem;
import dev.crius.dropcollector.collector.Collector;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CollectorSellEvent extends CollectorEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CollectedItem item;
    private final int amount;
    private final boolean auto;

    public CollectorSellEvent(Collector collector, CollectedItem item, int amount, boolean auto) {
        super(collector);
        this.item = item;
        this.amount = amount;
        this.auto = auto;
    }

    public CollectedItem getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isAuto() {
        return auto;
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
