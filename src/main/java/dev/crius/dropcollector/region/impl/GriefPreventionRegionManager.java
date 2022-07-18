package dev.crius.dropcollector.region.impl;

import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.region.RegionManager;
import lombok.RequiredArgsConstructor;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class GriefPreventionRegionManager implements RegionManager {

    private final DropCollectorPlugin plugin;

    @Override
    public String getName() {
        return "GriefPrevention";
    }

    @Override
    public boolean canManage(Player player, Collector collector) {
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(collector.getLocation(), true, null);
        if (claim == null) return false;
        if (claim.getOwnerID().equals(player.getUniqueId())) return true;

        return claim.getPermission(player.getName()) == ClaimPermission.Build;
    }

    @Override
    public Collector getCollector(Location location, CEntity entity) {
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
        if (claim == null) return null;

        for (Collector c : plugin.getCollectorManager().getCollectorsByEntity(entity)) {
            Claim cl = GriefPrevention.instance.dataStore.getClaimAt(c.getLocation(), true, null);
            if (cl == null) continue;
            if (!claim.equals(cl)) continue;

            return c;
        }

        return null;
    }

    @Override
    public Set<Collector> getCollectors(Location location) {
        Set<Collector> set = new HashSet<>();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
        if (claim == null) return set;

        for (Collector c : plugin.getCollectorManager().getCollectors()) {
            Claim cl = GriefPrevention.instance.dataStore.getClaimAt(c.getLocation(), true, null);
            if (cl == null) continue;
            if (!claim.equals(cl)) continue;

            set.add(c);
        }

        return set;
    }

    @EventHandler
    public void onDelete(ClaimDeletedEvent event) {
        getCollectors(event.getClaim().getGreaterBoundaryCorner()).forEach(plugin.getCollectorManager()::removeCollector);
    }

}
