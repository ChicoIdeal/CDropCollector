package dev.crius.dropcollector.region.impl;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.api.events.ClaimDeleteEvent;
import com.songoda.ultimateclaims.claim.Claim;
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
public class UltimateClaimsRegionManager implements RegionManager {

    private final DropCollectorPlugin plugin;

    @Override
    public String getName() {
        return "UltimateClaims";
    }

    @Override
    public boolean canManage(Player player, Collector collector) {
        Claim claim = UltimateClaims.getInstance().getClaimManager().getClaim(collector.getLocation().getChunk());
        if (claim == null) return false;

        return claim.isOwnerOrMember(player);
    }

    @Override
    public Collector getCollector(Location location, CEntity entity) {
        Claim claim = UltimateClaims.getInstance().getClaimManager().getClaim(location.getChunk());
        if (claim == null) return null;

        for (Collector c : plugin.getCollectorManager().getCollectorsByEntity(entity)) {
            Claim cl = UltimateClaims.getInstance().getClaimManager().getClaim(c.getLocation().getChunk());
            if (cl == null) continue;
            if (!claim.equals(cl)) continue;

            return c;
        }

        return null;
    }

    @Override
    public Set<Collector> getCollectors(Location location) {
        Set<Collector> set = new HashSet<>();
        Claim claim = UltimateClaims.getInstance().getClaimManager().getClaim(location.getChunk());
        if (claim == null) return set;

        for (Collector c : plugin.getCollectorManager().getCollectors()) {
            Claim cl = UltimateClaims.getInstance().getClaimManager().getClaim(c.getLocation().getChunk());
            if (cl == null) continue;
            if (!claim.equals(cl)) continue;

            set.add(c);
        }

        return set;
    }

    @EventHandler
    public void onDelete(ClaimDeleteEvent event) {
        getCollectors(event.getClaim().getHome()).forEach(plugin.getCollectorManager()::removeCollector);
    }

}
