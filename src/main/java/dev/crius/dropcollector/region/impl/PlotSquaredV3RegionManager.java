package dev.crius.dropcollector.region.impl;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.plotsquared.bukkit.events.PlotDeleteEvent;
import dev.crius.dropcollector.DropCollectorPlugin;
import dev.crius.dropcollector.collector.Collector;
import dev.crius.dropcollector.entity.CEntity;
import dev.crius.dropcollector.region.RegionManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
public class PlotSquaredV3RegionManager implements RegionManager {

    private static final PlotAPI PLOT_API = new PlotAPI();

    private final DropCollectorPlugin plugin;

    @Override
    public String getName() {
        return "PlotSquared v3";
    }

    @Override
    public boolean canManage(Player player, Collector collector) {
        com.intellectualcrafters.plot.object.Location location = toPSLocation(collector.getLocation());
        PlotArea plotArea = PLOT_API.getPlotSquared().getPlotAreaManager().getPlotArea(location);
        if (plotArea == null) return false;
        Plot plot = plotArea.getPlot(location);
        if (plot == null) return false;
        if (plugin.getPluginConfig().getBoolean("Settings.allow-coops-to-manage")
                && plot.getTrusted().contains(player.getUniqueId())) return true;

        return plot.isOwner(player.getUniqueId());
    }

    @Override
    public Collector getCollector(Location location, CEntity entity) {
        com.intellectualcrafters.plot.object.Location loc = toPSLocation(location);
        PlotArea plotArea = PLOT_API.getPlotSquared().getPlotAreaManager().getPlotArea(loc);
        if (plotArea == null) return null;
        Plot plot = plotArea.getPlot(loc);
        if (plot == null) return null;

        for (Collector collector : plugin.getCollectorManager().getCollectorsByEntity(entity)) {
            Location l = collector.getLocation();
            com.intellectualcrafters.plot.object.Location loc1 = toPSLocation(l);
            PlotArea pa = PLOT_API.getPlotSquared().getPlotAreaManager().getPlotArea(loc1);
            if (pa == null) continue;
            if (!plotArea.equals(pa)) continue;
            Plot p = plotArea.getPlot(loc1);
            if (!plot.equals(p)) continue;

            return collector;
        }

        return null;
    }

    @Override
    public Set<Collector> getCollectors(Location location) {
        Set<Collector> collectors = new HashSet<>();

        com.intellectualcrafters.plot.object.Location loc = toPSLocation(location);
        PlotArea plotArea = PLOT_API.getPlotSquared().getPlotAreaManager().getPlotArea(loc);
        if (plotArea == null) return collectors;
        Plot plot = plotArea.getPlot(loc);
        if (plot == null) return collectors;

        for (Collector collector : plugin.getCollectorManager().getCollectors()) {
            Location l = collector.getLocation();
            com.intellectualcrafters.plot.object.Location loc1 = toPSLocation(l);
            PlotArea pa = PLOT_API.getPlotSquared().getPlotAreaManager().getPlotArea(loc1);
            if (pa == null) continue;
            if (!plotArea.equals(pa)) continue;
            Plot p = plotArea.getPlot(loc1);
            if (!plot.equals(p)) continue;

            collectors.add(collector);
        }

        return collectors;
    }

    private com.intellectualcrafters.plot.object.Location toPSLocation(Location location) {
        return new com.intellectualcrafters.plot.object.Location(
                Objects.requireNonNull(location.getWorld()).getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    private Location toBukkitLocation(com.intellectualcrafters.plot.object.Location location) {
        return new Location(
                Bukkit.getWorld(location.getWorld()),
                location.getX(),
                location.getY(),
                location.getZ()
        );
    }

    @EventHandler
    public void onDelete(PlotDeleteEvent event) {
        getCollectors(toBukkitLocation(event.getPlot().getCenter())).forEach(plugin.getCollectorManager()::removeCollector);
    }

}
