package dev.crius.dropcollector.region;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.hook.Hook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Set;

public interface RegionManager extends Hook, Listener {

    String BYPASS_PERMISSION = "dropcollector.bypass";

    default void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DropCollectorPlugin.getInstance());
    }

    /**
     * @param collector The collector object
     * @param player The player to check
     * @return true if the player is allowed to manage false otherwise
     */
    boolean canManage(Player player, Collector collector);

    /**
     * @param collector The collector object
     * @param player The player whose attempting to create a collector
     * @return true if there are no collector with the same type on that region and if player is in the team. false otherwise
     */
    default Response canCreate(Player player, Collector collector) {
        if (!canManage(player, collector)) return Response.NO_PERMISSION;

        if (getCollector(collector.getLocation(), collector.getEntity()) != null) {
            return Response.ALREADY_EXISTS;
        }

        return Response.ALLOWED;
    }

    Collector getCollector(Location location, CEntity entity);

    Set<Collector> getCollectors(Location location);

    enum Response {
        ALLOWED, ALREADY_EXISTS, NO_PERMISSION
    }

}
