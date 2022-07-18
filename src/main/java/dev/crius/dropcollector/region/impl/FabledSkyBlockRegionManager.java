package dev.crius.dropcollector.region.impl;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.api.event.island.IslandDeleteEvent;
import com.songoda.skyblock.api.island.Island;
import com.songoda.skyblock.api.island.IslandEnvironment;
import com.songoda.skyblock.api.island.IslandRole;
import com.songoda.skyblock.api.island.IslandWorld;
import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.region.RegionManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class FabledSkyBlockRegionManager implements RegionManager {

    private final DropCollectorPlugin plugin;

    @Override
    public String getName() {
        return "FabledSkyBlock";
    }

    @Override
    public boolean canManage(Player player, Collector collector) {
        Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(collector.getLocation());
        if (island == null || island.getIsland() == null) return false;
        if (island.getCoopPlayers().containsKey(player.getUniqueId())
                && plugin.getPluginConfig().getBoolean("Settings.allow-coops-to-manage")) return true;
        if (island.getOwnerUUID().equals(player.getUniqueId())) return true;
        if (island.hasRole(player.getUniqueId(), IslandRole.OPERATOR)) return true;

        return island.hasRole(player.getUniqueId(), IslandRole.MEMBER);
    }

    @Override
    public Collector getCollector(Location location, CEntity entity) {
        Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(location);
        if (island == null) return null;

        for (Collector c : plugin.getCollectorManager().getCollectorsByEntity(entity)) {
            Island i = SkyBlockAPI.getIslandManager().getIslandAtLocation(c.getLocation());
            if (i == null) continue;
            if (!island.equals(i)) continue;

            return c;
        }

        return null;
    }

    @Override
    public Set<Collector> getCollectors(Location location) {
        Set<Collector> set = new HashSet<>();
        Island island = SkyBlockAPI.getIslandManager().getIslandAtLocation(location);
        if (island == null) return set;

        for (Collector c : plugin.getCollectorManager().getCollectors()) {
            Island i = SkyBlockAPI.getIslandManager().getIslandAtLocation(c.getLocation());
            if (i == null) continue;
            if (!island.equals(i)) continue;

            set.add(c);
        }

        return set;
    }

    @EventHandler
    public void onDelete(IslandDeleteEvent event) {
        getCollectors(event.getIsland().getLocation(IslandWorld.OVERWORLD, IslandEnvironment.MAIN))
                .forEach(plugin.getCollectorManager()::removeCollector);
    }

}
