package dev.crius.dropcollector.database;

import dev.crius.dropcollector.collector.Collector;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public interface Database {

    void onEnable();

    void saveAll();

    void saveCollector(Collector collector);

    Collection<Collector> getCollectors();

    Collection<Collector> getCollectors(UUID uuid);

    default Collection<Collector> getCollectors(Player player) {
        return getCollectors(player.getUniqueId());
    }

    void remove(Collector collector);

    void removeAll();

}
