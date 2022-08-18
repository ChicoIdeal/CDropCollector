package dev.crius.dropcollector.region.impl;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.region.RegionManager;
import lombok.RequiredArgsConstructor;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class LandsRegionManager implements RegionManager {

    private static final LandsIntegration LANDS = new LandsIntegration(DropCollectorPlugin.getInstance());

    private final DropCollectorPlugin plugin;

    @Override
    public String getName() {
        return "Lands";
    }

    @Override
    public boolean canManage(Player player, Collector collector) {
        Area area = LANDS.getAreaByLoc(collector.getLocation());
        if (area == null) return false;
        if (area.getOwnerUID().equals(player.getUniqueId())) return true;

        return area.isTrusted(player.getUniqueId());
    }

    @Override
    public Collector getCollector(Location location, CEntity entity) {
        Area area = LANDS.getAreaByLoc(location);
        if (area == null) return null;

        for (Collector c : plugin.getCollectorManager().getCollectorsByEntity(entity)) {
            Area a = LANDS.getAreaByLoc(c.getLocation());
            if (a == null) continue;
            if (!area.equals(a)) continue;

            return c;
        }

        return null;
    }

    @Override
    public Set<Collector> getCollectors(Location location) {
        Set<Collector> set = new HashSet<>();
        Area area = LANDS.getAreaByLoc(location);
        if (area == null) return set;

        for (Collector c : plugin.getCollectorManager().getCollectors()) {
            Area a = LANDS.getAreaByLoc(c.getLocation());
            if (a == null) continue;
            if (!area.equals(a)) continue;

            set.add(c);
        }

        return set;
    }
}
