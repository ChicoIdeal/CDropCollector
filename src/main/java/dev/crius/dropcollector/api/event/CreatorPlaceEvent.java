package dev.crius.dropcollector.api.event;

import dev.crius.dropcollector.collector.Collector;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CreatorPlaceEvent extends CollectorEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;

    public CreatorPlaceEvent(@NotNull Player who, Collector collector) {
        super(collector);
        this.player = who;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
