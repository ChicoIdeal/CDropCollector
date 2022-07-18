package dev.crius.dropcollector.region.impl;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.region.RegionManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.events.island.IslandDeleteEvent;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.RanksManager;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class BentoBoxRegionManager implements RegionManager {

    private final DropCollectorPlugin plugin;
    private final IslandsManager islandsManager = BentoBox.getInstance().getIslands();

    @Override
    public String getName() {
        return "BentoBox";
    }

    @Override
    public boolean canManage(Player player, Collector collector) {
        Island island = islandsManager.getIslandAt(collector.getLocation()).orElse(null);

        if (island == null) return false;
        if (island.getOwner() == null) return false;
        if (island.getOwner().equals(player.getUniqueId())) return true;
        if (island.getMemberSet(RanksManager.COOP_RANK).contains(player.getUniqueId())
                && plugin.getPluginConfig().getBoolean("Settings.allow-coops-to-manage")) return true;

        return island.getMemberSet(RanksManager.MEMBER_RANK).contains(player.getUniqueId());
    }

    @Override
    public Collector getCollector(Location location, CEntity entity) {
        Island island = islandsManager.getIslandAt(location).orElse(null);
        if (island == null) return null;

        for (Collector c : plugin.getCollectorManager().getCollectorsByEntity(entity)) {
            Island i = islandsManager.getIslandAt(c.getLocation()).orElse(null);
            if (i == null) continue;
            if (!island.equals(i)) continue;

            return c;
        }

        return null;
    }

    @Override
    public Set<Collector> getCollectors(Location location) {
        Set<Collector> set = new HashSet<>();
        Island island = islandsManager.getIslandAt(location).orElse(null);
        if (island == null) return set;

        for (Collector c : plugin.getCollectorManager().getCollectors()) {
            Island i = islandsManager.getIslandAt(c.getLocation()).orElse(null);
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
