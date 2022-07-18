package dev.crius.dropcollector.region.impl;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
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
public class SuperiorSkyBlockRegionManager implements RegionManager {

    private final DropCollectorPlugin plugin;

    @Override
    public String getName() {
        return "SuperiorSkyblock2";
    }

    @Override
    public boolean canManage(Player player, Collector collector) {
        Island island = SuperiorSkyblockAPI.getIslandAt(collector.getLocation());
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
        if (island == null || superiorPlayer == null) return false;
        if (island.isCoop(superiorPlayer)
                && plugin.getPluginConfig().getBoolean("Settings.allow-coops-to-manage")) return true;

        // since isMember method checks the owner, we don't need to check for the owner
        return island.isMember(superiorPlayer);
    }

    @SuppressWarnings("ConstantConditions") // canManage method already checks for null so no need to check twice.
    @Override
    public Collector getCollector(Location location, CEntity entity) {
        Island island = SuperiorSkyblockAPI.getIslandAt(location);

        for (Collector c : plugin.getCollectorManager().getCollectorsByEntity(entity)) {
            Island i = SuperiorSkyblockAPI.getIslandAt(c.getLocation());
            if (i == null) continue;
            if (!island.equals(i)) continue;

            return c;
        }

        return null;
    }

    @Override
    public Set<Collector> getCollectors(Location location) {
        Set<Collector> set = new HashSet<>();
        Island island = SuperiorSkyblockAPI.getIslandAt(location);
        if (island == null) return set;

        for (Collector c : plugin.getCollectorManager().getCollectors()) {
            Island i = SuperiorSkyblockAPI.getIslandAt(c.getLocation());
            if (i == null) continue;
            if (!island.equals(i)) continue;

            set.add(c);
        }

        return set;
    }

    @EventHandler
    public void onDelete(IslandDisbandEvent event) {
        getCollectors(event.getIsland().getMinimumProtected()).forEach(plugin.getCollectorManager()::removeCollector);
    }

}
