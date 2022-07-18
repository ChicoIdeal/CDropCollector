package dev.crius.dropcollector.region.impl;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.region.RegionManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class ChunkBasedRegionManager implements RegionManager {

    private final DropCollectorPlugin plugin;

    @Override
    public String getName() {
        return "Chunk Based Region Manager";
    }

    @Override
    public boolean canManage(Player player, Collector collector) {
        return collector.getOwner().equals(player.getUniqueId());
    }

    @Override
    public Collector getCollector(Location location, CEntity entity) {
        Chunk chunk = location.getChunk();
        for (Collector c : plugin.getCollectorManager().getCollectorsByEntity(entity)) {
            if (c.getLocation().getChunk().equals(chunk)) return c;
        }

        return null;
    }

    @Override
    public Set<Collector> getCollectors(Location location) {
        Set<Collector> set = new HashSet<>();

        Chunk chunk = location.getChunk();
        for (Collector c : plugin.getCollectorManager().getCollectors()) {
            if (c.getLocation().getChunk().equals(chunk)) set.add(c);
        }

        return set;
    }

}
