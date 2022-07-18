package dev.crius.dropcollector.api.event;

import dev.crius.dropcollector.collector.Collector;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class CollectorEvent extends Event implements Cancellable {

    private final Collector collector;

    private boolean cancelled;

    public CollectorEvent(Collector collector) {
        this.collector = collector;
    }

    public Collector getCollector() {
        return collector;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
