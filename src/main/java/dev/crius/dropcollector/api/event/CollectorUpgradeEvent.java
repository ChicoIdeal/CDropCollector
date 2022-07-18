package dev.crius.dropcollector.api.event;

import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.upgrade.Upgrade;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CollectorUpgradeEvent extends CollectorEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Upgrade upgrade;

    public CollectorUpgradeEvent(Collector collector, Upgrade upgrade) {
        super(collector);
        this.upgrade = upgrade;
    }

    public Upgrade getUpgrade() {
        return upgrade;
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
