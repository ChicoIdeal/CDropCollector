package dev.crius.dropcollector.region.impl;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.api.event.RemoveClaimEvent;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.region.RegionManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class GriefDefenderRegionManager implements RegionManager {

    private final DropCollectorPlugin plugin;

    @Override
    public void init() {
        GriefDefender.getEventManager().getBus().subscribe(RemoveClaimEvent.class, event -> {
            World world = Bukkit.getWorld(event.getClaim().getClaimManager().getWorldId());
            Vector3i vector = event.getClaim().getGreaterBoundaryCorner();
            Location location = new Location(world, vector.getX(), vector.getY(), vector.getZ());
            getCollectors(location).forEach(plugin.getCollectorManager()::removeCollector);
        });
    }

    @Override
    public String getName() {
        return "GriefDefender";
    }

    @Override
    public boolean canManage(Player player, Collector collector) {
        Claim claim = GriefDefender.getCore().getClaimAt(collector.getLocation());
        if (claim == null || claim.isWilderness()) return false;
        if (claim.getOwnerUniqueId().equals(player.getUniqueId())) return true;

        return claim.isUserTrusted(player.getUniqueId(), TrustTypes.MANAGER);
    }

    @Override
    public Collector getCollector(Location location, CEntity entity) {
        Claim claim = GriefDefender.getCore().getClaimAt(location);
        if (claim == null) return null;

        for (Collector collector : plugin.getCollectorManager().getCollectorsByEntity(entity)) {
            Claim cl = GriefDefender.getCore().getClaimAt(collector.getLocation());
            if (cl == null) continue;
            if (!claim.equals(cl)) continue;

            return collector;
        }

        return null;
    }

    @Override
    public Set<Collector> getCollectors(Location location) {
        Set<Collector> set = new HashSet<>();
        Claim claim = GriefDefender.getCore().getClaimAt(location);
        if (claim == null) return set;

        for (Collector collector : plugin.getCollectorManager().getCollectors()) {
            Claim cl = GriefDefender.getCore().getClaimAt(collector.getLocation());
            if (cl == null) continue;
            if (!claim.equals(cl)) continue;

            set.add(collector);
        }

        return set;
    }

}
