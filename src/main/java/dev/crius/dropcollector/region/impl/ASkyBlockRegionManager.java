package dev.crius.dropcollector.region.impl;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.CoopPlay;
import com.wasteofplastic.askyblock.Island;
import com.wasteofplastic.askyblock.events.IslandDeleteEvent;
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
public class ASkyBlockRegionManager implements RegionManager {

    private final DropCollectorPlugin plugin;

    @Override
    public String getName() {
        return "ASkyBlock";
    }

    @Override
    public boolean canManage(Player player, Collector collector) {
        Island island = ASkyBlockAPI.getInstance().getIslandAt(collector.getLocation());
        if (island == null) return false;
        if (island.getOwner().equals(player.getUniqueId())) return true;
        if (CoopPlay.getInstance().getCoopPlayers(island.getCenter()).contains(player.getUniqueId())
                && plugin.getPluginConfig().getBoolean("Settings.allow-coops-to-manage")) return true;

        return island.getMembers().contains(player.getUniqueId());
    }

    @Override
    public Collector getCollector(Location location, CEntity entity) {
        Island island = ASkyBlockAPI.getInstance().getIslandAt(location);
        if (island == null) return null;

        for (Collector c : plugin.getCollectorManager().getCollectorsByEntity(entity)) {
            Island i = ASkyBlockAPI.getInstance().getIslandAt(c.getLocation());
            if (i == null) continue;
            if (!island.equals(i)) continue;

            return c;
        }

        return null;
    }

    @Override
    public Set<Collector> getCollectors(Location location) {
        Set<Collector> set = new HashSet<>();
        Island island = ASkyBlockAPI.getInstance().getIslandAt(location);
        if (island == null) return set;

        for (Collector c : plugin.getCollectorManager().getCollectors()) {
            Island i = ASkyBlockAPI.getInstance().getIslandAt(c.getLocation());
            if (i == null) continue;
            if (!island.equals(i)) continue;

            set.add(c);
        }

        return set;
    }

    @EventHandler
    public void onDelete(IslandDeleteEvent event) {
        getCollectors(event.getLocation()).forEach(plugin.getCollectorManager()::removeCollector);
    }

}
